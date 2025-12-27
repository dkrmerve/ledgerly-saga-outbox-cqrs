terraform {
  required_version = ">= 1.4.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# -------------------------------------------------------------------
# DESIGN ONLY
# This Terraform configuration is intentionally NOT runnable.
# It exists solely to demonstrate AWS infrastructure awareness.
# -------------------------------------------------------------------

provider "aws" {
  region = "eu-west-1"
}
