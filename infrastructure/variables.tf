variable "docker_image" {
  type        = string
  description = "Full Docker image reference (e.g. dockerhub_username/epita-cloud-backend:latest) that new instances pull on boot."
}

variable "dockerhub_username" {
  type        = string
  description = "Docker Hub username used to authenticate the image pull on instance boot (the repo is private)."
}

variable "dockerhub_token" {
  type        = string
  description = "Docker Hub access token used to authenticate the image pull on instance boot."
  sensitive   = true
}
