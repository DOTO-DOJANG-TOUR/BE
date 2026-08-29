data "aws_ssm_parameter" "ubuntu_2404_ami" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

resource "aws_iam_role" "ec2" {
  name               = "${var.project_name}-${var.environment}-ec2-role"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{ Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" }, Action = "sts:AssumeRole" }] })
}

resource "aws_iam_role_policy" "ec2_runtime" {
  name = "ecr-pull"
  role = aws_iam_role.ec2.id
  policy = jsonencode({ Version = "2012-10-17", Statement = [
    { Effect = "Allow", Action = "ecr:GetAuthorizationToken", Resource = "*" },
    { Effect = "Allow", Action = ["ecr:BatchCheckLayerAvailability", "ecr:GetDownloadUrlForLayer", "ecr:BatchGetImage"], Resource = aws_ecr_repository.app.arn }
  ] })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${var.project_name}-${var.environment}-ec2-profile"
  role = aws_iam_role.ec2.name
}

resource "aws_ebs_volume" "data" {
  availability_zone = aws_subnet.public.availability_zone
  size              = var.ebs_volume_size_gb
  type              = "gp3"
  encrypted         = true
  tags              = { Name = "${var.project_name}-${var.environment}-data" }

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.ubuntu_2404_ami.value
  instance_type               = "t3.small"
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  key_name                    = var.key_name
  associate_public_ip_address = true
  # 기존 서버 교체 방지
  user_data_replace_on_change = false

  user_data = <<-USERDATA
    #!/usr/bin/env bash
    set -euxo pipefail
    export DEBIAN_FRONTEND=noninteractive
    apt-get update
    apt-get install -y ca-certificates curl unzip
    curl -fsSL https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o /tmp/awscliv2.zip
    unzip -q /tmp/awscliv2.zip -d /tmp
    /tmp/aws/install --update
    rm -rf /tmp/aws /tmp/awscliv2.zip
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=$$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $$(. /etc/os-release && echo \"$$VERSION_CODENAME\") stable" > /etc/apt/sources.list.d/docker.list
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl enable --now docker
    usermod -aG docker ubuntu
    if ! swapon --noheadings --show=NAME | grep -qx /swapfile; then
      fallocate -l 2G /swapfile
      chmod 600 /swapfile
      mkswap /swapfile
      swapon /swapfile
      echo '/swapfile none swap sw 0 0' >> /etc/fstab
    fi
    VOLUME_SERIAL="${replace(aws_ebs_volume.data.id, "-", "")}"; DEVICE=""
    for _ in $$(seq 1 60); do
      DEVICE=$$(lsblk -ndo NAME,SERIAL | awk -v serial="$$VOLUME_SERIAL" '$$2 == serial { print "/dev/" $$1; exit }')
      [ -n "$$DEVICE" ] && break; sleep 2
    done
    test -n "$$DEVICE"
    blkid "$$DEVICE" >/dev/null 2>&1 || mkfs.ext4 -F "$$DEVICE"
    mkdir -p /data /opt/app
    UUID=$$(blkid -s UUID -o value "$$DEVICE")
    grep -qE "^[^#]*[[:space:]]/data[[:space:]]" /etc/fstab || echo "UUID=$$UUID /data ext4 defaults,nofail 0 2" >> /etc/fstab
    mount -a
    mkdir -p /data/db
    chown 999:999 /data/db

    touch /opt/app/.env
    chown root:root /opt/app/.env
    chmod 600 /opt/app/.env
    cat > /etc/systemd/system/doto-compose.service <<'UNIT'
    [Unit]
    Description=DOTO Docker Compose
    Requires=docker.service
    After=docker.service network-online.target
    [Service]
    Type=oneshot
    RemainAfterExit=yes
    WorkingDirectory=/opt/app
    ExecStart=/usr/bin/docker compose --env-file /opt/app/.env up -d --remove-orphans
    ExecStop=/usr/bin/docker compose --env-file /opt/app/.env down
    TimeoutStartSec=0
    [Install]
    WantedBy=multi-user.target
    UNIT
    systemctl daemon-reload
    systemctl enable doto-compose.service

    cat > /usr/local/bin/doto-renew-certificates <<'RENEW'
    #!/usr/bin/env bash
    set -euo pipefail
    enable_tls=$$(sed -n 's/^ENABLE_TLS=//p' /opt/app/.env | tail -n 1)
    [ "$$enable_tls" = "true" ] || exit 0
    docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml run --rm certbot renew --webroot -w /var/www/certbot --quiet
    docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml exec -T nginx nginx -s reload
    RENEW
    chmod 700 /usr/local/bin/doto-renew-certificates
    cat > /etc/systemd/system/doto-certbot.service <<'UNIT'
    [Unit]
    Description=Renew DOTO Let's Encrypt certificates
    After=docker.service doto-compose.service
    [Service]
    Type=oneshot
    ExecStart=/usr/local/bin/doto-renew-certificates
    UNIT
    cat > /etc/systemd/system/doto-certbot.timer <<'UNIT'
    [Unit]
    Description=Run DOTO certificate renewal twice each day
    [Timer]
    OnCalendar=*-*-* 03,15:00:00
    Persistent=true
    [Install]
    WantedBy=timers.target
    UNIT
    systemctl daemon-reload
    systemctl enable --now doto-certbot.timer
  USERDATA

  tags = { Name = "${var.project_name}-${var.environment}-app" }
}

resource "aws_volume_attachment" "data" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.data.id
  instance_id = aws_instance.app.id
}

resource "aws_eip" "app" {
  domain = "vpc"
  tags   = { Name = "${var.project_name}-${var.environment}-eip" }
}

resource "aws_eip_association" "app" {
  allocation_id = aws_eip.app.id
  instance_id   = aws_instance.app.id
}
