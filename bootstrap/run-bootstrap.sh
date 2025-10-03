#!/bin/bash
# Script para executar o bootstrap do backend Terraform

set -e

echo "=================================================="
echo "Bootstrap do Backend Terraform"
echo "=================================================="
echo ""

# Verificar se está no diretório correto
if [ ! -f "main.tf" ]; then
    echo "❌ Erro: Execute este script de dentro do diretório bootstrap/"
    exit 1
fi

# Verificar credenciais AWS
if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    echo "⚠️  Aviso: AWS_ACCESS_KEY_ID não configurada"
    echo "Configure suas credenciais AWS antes de continuar:"
    echo ""
    echo "  export AWS_ACCESS_KEY_ID='sua-key'"
    echo "  export AWS_SECRET_ACCESS_KEY='sua-secret'"
    echo "  export AWS_SESSION_TOKEN='seu-token'  # Se usar credenciais temporárias"
    echo ""
    read -p "Pressione ENTER para continuar ou CTRL+C para cancelar..."
fi

echo "📦 Passo 1: Inicializando Terraform..."
terraform init

echo ""
echo "📋 Passo 2: Verificando o que será criado..."
terraform plan

echo ""
read -p "Deseja criar os recursos acima? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Cancelado pelo usuário"
    exit 0
fi

echo ""
echo "🚀 Passo 3: Criando recursos..."
terraform apply -auto-approve

echo ""
echo "✅ Bootstrap concluído com sucesso!"
echo ""
echo "Recursos criados:"
echo "  - S3 Bucket: tech-challenge-tfstate-533267363894"
echo "  - DynamoDB Table: tech-challenge-terraform-lock-533267363894"
echo ""
echo "⚠️  IMPORTANTE: Faça backup do arquivo terraform.tfstate!"
echo ""
echo "Agora você pode executar o GitHub Actions no repositório principal."
