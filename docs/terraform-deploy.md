# Terraform 기반 EC2 배포 가이드

## 구성

`infra/`는 서울 리전의 단일 `t3.small` EC2, public subnet, Elastic IP와 별도 gp3 EBS 볼륨을 생성합니다. RDS는 사용하지 않습니다. EBS는 `/data`에 마운트되고 PostgreSQL 데이터는 `/data/db`에 bind mount됩니다.

Terraform은 ECR 저장소와 GitHub Actions OIDC Role을 함께 생성합니다. ECR은 이미지 push 시 취약점 스캔을 실행하고, 태그 변경을 막으며, 최근 20개 이미지만 보관합니다.

외부에서 열리는 포트는 Nginx의 HTTP(80)와 HTTPS(443)뿐입니다. 앱(8080)과 PostgreSQL(5432)은 Docker 내부 네트워크만 사용합니다. Nginx와 Certbot 컨테이너가 Let's Encrypt 인증서를 발급·갱신하며, 인증서는 EBS의 `/data/letsencrypt`에 보관합니다.

## 사전 준비

1. 서울 리전의 EC2 Key Pair를 준비합니다.
2. GitHub repository와 동일한 이름이 아니라면 `terraform.tfvars`에 `github_repository = "owner/repository"`를 추가합니다.

## Terraform 실행

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars에 Key Pair, 내 공인 IP/32을 입력
terraform init
terraform plan
terraform apply
terraform output -raw ecr_repository_url
terraform output -raw github_actions_deploy_role_arn
```

Ubuntu 24.04 LTS AMI는 Canonical의 공개 SSM 파라미터에서 자동으로 최신 버전을 조회하므로 AMI ID를 직접 입력할 필요가 없습니다.

## 서버 환경 변수 설정

완료 후 SSH로 접속해 시크릿 파일을 생성합니다. [`../deploy/.env.example`](../deploy/.env.example)을 참고하되, 실제 값은 서버에만 두며 GitHub, Terraform state, 저장소에는 넣지 않습니다. Docker Compose는 이 파일의 `POSTGRES_PASSWORD`를 PostgreSQL과 앱 컨테이너 모두에 환경변수로 주입합니다.

```bash
sudo nano /opt/app/.env
# POSTGRES_PASSWORD=...
# JWT_SECRET=...
# TOUR_API_SERVICE_KEY=...
# GOOGLE_OIDC_CLIENT_ID=...
# KAKAO_OIDC_CLIENT_ID=...
sudo chmod 600 /opt/app/.env
```

## 도메인과 HTTPS 설정

도메인 DNS의 A 레코드를 Elastic IP로 연결하기 전에는 `.env`의 `ENABLE_TLS=false`를 유지합니다. DNS 전파가 확인되면 아래 항목을 입력하고 `ENABLE_TLS=true`로 변경한 뒤 다음 GitHub Actions 배포를 실행합니다. 배포 과정에서 Certbot이 인증서를 발급하고 Nginx를 HTTPS로 전환합니다.

```text
DOMAIN_NAME=doto-app.cloud
LETSENCRYPT_EMAIL=your-email@example.com
ENABLE_TLS=true
```

## GitHub Actions 설정

GitHub의 **Settings → Environments → dev**에서 deployment branch를 `develop`으로 제한하고 아래 값을 Secret으로 설정합니다. `.tfvars`와 `.env`는 Git에서 제외됩니다.

| 종류 | 이름 | 값 |
|---|---|---|
| Secret | `AWS_GITHUB_ACTIONS_ROLE_ARN` | `terraform output -raw github_actions_deploy_role_arn` |
| Secret | `DEV_HOST` | `terraform output -raw elastic_ip` |
| Secret | `DEV_USERNAME` | `ubuntu` |
| Secret | `DEV_PRIVATE_KEY` | `doto-key.pem` 파일의 전체 내용 |

## 배포와 확인

`develop` push 뒤 CI가 성공하면 `.github/workflows/cd.yml`이 이미지를 ECR에 push하고, Compose 파일과 배포 스크립트를 SSH로 EC2에 전송합니다. 서버에서 ECR image pull, Docker Compose 재시작, health check까지 실행하며 어느 단계라도 실패하면 GitHub workflow가 실패합니다.

```bash
# 서버 접속
ssh ubuntu@$(cd infra && terraform output -raw elastic_ip)

# 컨테이너 및 서비스 상태
sudo systemctl status doto-compose.service
sudo docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml ps
curl -fsS http://localhost/actuator/health

# 외부 경로 확인
curl -i http://<ELASTIC_IP>/actuator/health
```

## 데이터 보호

DB EBS에는 `prevent_destroy`가 설정되어 있어 `terraform destroy`를 실행해도 Terraform이 볼륨 삭제를 거부합니다. 이는 `/data/db` 데이터의 실수 삭제를 막기 위한 보호장치입니다. 장애 복구를 위해서는 별도로 정기 EBS Snapshot을 운영하세요.
