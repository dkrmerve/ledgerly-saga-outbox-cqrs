# VPC & Networking (Design Only)

resource "aws_vpc" "ledgerly" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "ledgerly-vpc"
  }
}

# In production:
# - Public subnets: ALB
# - Private subnets: ECS, RDS, MSK, Redis
# - No direct internet access from DB layers
