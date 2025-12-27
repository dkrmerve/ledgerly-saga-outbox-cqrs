# ECS Fargate (Design Only)

resource "aws_ecs_cluster" "ledgerly" {
  name = "ledgerly-cluster"
}

# API Service
resource "aws_ecs_service" "api" {
  name            = "ledgerly-api"
  cluster         = aws_ecs_cluster.ledgerly.id
  launch_type     = "FARGATE"
  desired_count   = 2

  # Task definition omitted intentionally
}

# Worker Service (Saga + Outbox Publisher)
resource "aws_ecs_service" "worker" {
  name            = "ledgerly-worker"
  cluster         = aws_ecs_cluster.ledgerly.id
  launch_type     = "FARGATE"
  desired_count   = 2

  # Runs:
  # - Saga orchestrator
  # - Outbox publisher
}

# Notes:
# - API behind ALB
# - Workers without public exposure
# - Horizontal scaling safe due to SKIP LOCKED + inbox
