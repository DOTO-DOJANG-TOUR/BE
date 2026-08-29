variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}
variable "project_name" {
  type    = string
  default = "doto"
}
variable "environment" {
  type    = string
  default = "dev"
}
variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}
variable "public_subnet_cidr" {
  type    = string
  default = "10.0.1.0/24"
}
variable "availability_zone" {
  type    = string
  default = "ap-northeast-2a"
}
variable "key_name" {
  type        = string
  description = "EC2 SSH key pair name"
}
variable "ssh_allowed_cidr" {
  type        = string
  description = "현재 공인 IP CIDR. 예: 203.0.113.10/32"
  validation {
    condition     = can(cidrhost(var.ssh_allowed_cidr, 0))
    error_message = "CIDR 형식이어야 합니다."
  }
}
variable "ebs_volume_size_gb" {
  type    = number
  default = 20
}
