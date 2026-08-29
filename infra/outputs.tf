output "elastic_ip" { value = aws_eip.app.public_ip }
output "instance_id" { value = aws_instance.app.id }
output "data_volume_id" { value = aws_ebs_volume.data.id }
output "ssh_command" { value = "ssh ubuntu@${aws_eip.app.public_ip}" }
output "ecr_repository_url" { value = aws_ecr_repository.app.repository_url }
output "github_actions_deploy_role_arn" { value = aws_iam_role.github_actions_deploy.arn }
