# Bootstrap do Backend Terraform

## ⚠️ IMPORTANTE: Execute isso ANTES de rodar o GitHub Actions!

Este diretório cria os recursos necessários para o backend remoto do Terraform:
- **S3 Bucket**: Para armazenar o state
- **DynamoDB Table**: Para lock do state

## Como Executar

### 1. Configure suas credenciais AWS localmente

```bash
# Opção 1: Usando variáveis de ambiente
export AWS_ACCESS_KEY_ID="sua-access-key"
export AWS_SECRET_ACCESS_KEY="sua-secret-key"
export AWS_SESSION_TOKEN="seu-session-token"  # Se usar credenciais temporárias

# Opção 2: Usando AWS CLI configure
aws configure
```

### 2. Execute o bootstrap

```bash
cd bootstrap

# Inicializar
terraform init

# Ver o que será criado
terraform plan

# Criar os recursos
terraform apply
```

### 3. Após a criação bem-sucedida

Agora você pode executar o workflow do GitHub Actions no repositório principal, pois o bucket S3 e a tabela DynamoDB já existirão!

## Recursos Criados

- **S3 Bucket**: `tech-challenge-tfstate-533267363894`
- **DynamoDB Table**: `tech-challenge-terraform-lock-533267363894`

## ⚠️ Atenção

- Este bootstrap usa **state local** (arquivo `terraform.tfstate` local)
- **NÃO DELETE** este state file! Você precisará dele para destruir os recursos depois
- Faça backup do arquivo `terraform.tfstate` após executar
