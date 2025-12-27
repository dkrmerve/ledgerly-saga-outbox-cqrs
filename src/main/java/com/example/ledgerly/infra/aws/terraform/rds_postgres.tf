# RDS PostgreSQL (Design Only)

resource "aws_db_instance" "postgres" {
  identifier = "ledgerly-postgres"

  engine         = "postgres"
  engine_version = "15"
  instance_class = "db.t3.medium"

  multi_az = true

  allocated_storage = 100

  # username/password should come from AWS Secrets Manager
  # never hard-coded in Terraform state

  publicly_accessible = false

  tags = {
    Service = "ledgerly"
    Role    = "primary-database"
  }
}

# Notes:
# - Multi-AZ for HA
# - Automated backups enabled
# - Encryption with KMS
