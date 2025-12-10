# Sistema SaaS Multi-Tenant - Documentação

## Visão Geral

Este projeto foi transformado em uma plataforma SaaS (Software as a Service) completa com suporte multi-tenant, permitindo que múltiplas organizações utilizem o sistema de forma isolada e segura.

## Arquitetura Multi-Tenant

### Isolamento de Dados

O sistema implementa um modelo de **multi-tenancy com isolamento por tenant_id**, onde:

- Cada registro no banco de dados possui um campo `tenant_id`
- Filtros automáticos do Hibernate garantem que queries só retornem dados do tenant correto
- Cada requisição deve incluir o header `X-Tenant-ID` (exceto endpoints públicos)

### Componentes Principais

#### 1. **TenantContext** (`/context/TenantContext.java`)
- Armazena o tenant_id atual usando ThreadLocal
- Limpo automaticamente após cada requisição

#### 2. **TenantInterceptor** (`/interceptor/TenantInterceptor.java`)
- Intercepta todas as requisições
- Extrai o `X-Tenant-ID` do header
- Valida se o tenant está ativo
- Define o contexto do tenant para a requisição

#### 3. **TenantAware** (`/model/TenantAware.java`)
- Classe base para todas as entidades multi-tenant
- Adiciona automaticamente o campo `tenant_id`
- Configura filtros Hibernate para isolamento de dados

#### 4. **TenantEntityListener** (`/config/TenantEntityListener.java`)
- Listener JPA que define automaticamente o tenant_id ao criar/atualizar entidades
- Impede mudanças acidentais de tenant

## Registro de Novos Tenants

### Endpoint de Registro

**POST** `/api/tenants/register` (público, não requer autenticação)

```json
{
  "name": "Nome da Organização",
  "subdomain": "minha-org",
  "adminUsername": "admin",
  "adminName": "Administrador",
  "adminEmail": "admin@example.com",
  "adminPassword": "senha123",
  "city": "São Paulo",
  "state": "SP"
}
```

### O que acontece no registro:

1. **Validação do subdomínio** (3-63 caracteres, apenas letras minúsculas, números e hífens)
2. **Criação do Tenant** com status `TRIAL`
3. **Criação automática de:**
   - Role ADMIN com todas as permissões
   - Plano Trial (30 dias, 5 usuários, 50 documentos, 500MB)
   - Assinatura Trial ativa por 30 dias
   - Usuário administrador com role ADMIN

## Planos e Assinaturas

### Estrutura de Planos

Cada plano define limites de recursos:

- **maxUsers**: Número máximo de usuários
- **maxDocuments**: Número máximo de documentos
- **maxStorageMb**: Armazenamento máximo em MB
- **price**: Valor mensal

### Planos Padrão

#### Trial (Criado automaticamente)
- Duração: 30 dias
- Usuários: 5
- Documentos: 50
- Storage: 500 MB
- Preço: Gratuito

### Gerenciamento de Assinaturas

**PUT** `/api/subscriptions/{tenantId}/plan/{planId}` (requer autenticação)

Atualiza o plano da assinatura do tenant:
- Cancela assinatura atual
- Cria nova assinatura com o novo plano
- Atualiza status do tenant

## Autenticação com Tenant

### Login

**POST** `/api/auth/login`

```json
{
  "username": "admin",
  "password": "senha123",
  "tenantId": 1
}
```

O sistema valida:
1. Se o tenant existe e está ativo
2. Se as credenciais estão corretas
3. Se o usuário pertence ao tenant especificado

### Resposta de Login

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "name": "Administrador",
  "role": "ADMIN",
  "tenantId": 1,
  "tenantName": "Nome da Organização"
}
```

## Validação de Limites do Plano

### PlanLimitService

O serviço `PlanLimitService` valida automaticamente os limites antes de criar recursos:

#### Validação de Usuários
```java
planLimitService.validateUserCreation();
```

#### Validação de Documentos
```java
planLimitService.validateDocumentCreation();
```

#### Validação de Storage
```java
planLimitService.validateStorageLimit(fileSizeBytes);
```

### Estatísticas de Uso

**GET** `/api/usage/statistics`

Retorna uso atual vs limites do plano:

```json
{
  "userCount": 3,
  "maxUsers": 5,
  "documentCount": 25,
  "maxDocuments": 50,
  "storageMb": 150,
  "maxStorageMb": 500,
  "userPercentage": 60.0,
  "documentPercentage": 50.0,
  "storagePercentage": 30.0
}
```

## Endpoints Administrativos

### Listar Tenants
**GET** `/api/tenants` (requer role ADMIN)

### Obter Tenant por ID
**GET** `/api/tenants/{id}` (requer role ADMIN)

### Obter Tenant por Subdomínio
**GET** `/api/tenants/subdomain/{subdomain}`

### Suspender Tenant
**PUT** `/api/tenants/{id}/suspend` (requer role ADMIN)

### Ativar Tenant
**PUT** `/api/tenants/{id}/activate` (requer role ADMIN)

### Cancelar Tenant
**PUT** `/api/tenants/{id}/cancel` (requer role ADMIN)

## Estados do Tenant

### TRIAL
- Tenant em período de avaliação (30 dias)
- Acesso completo às funcionalidades
- Limites do plano Trial aplicados

### ACTIVE
- Tenant com assinatura paga ativa
- Acesso completo às funcionalidades
- Limites do plano contratado aplicados

### SUSPENDED
- Acesso temporariamente bloqueado
- Dados preservados
- Login negado

### CANCELLED
- Assinatura cancelada
- Acesso bloqueado
- Dados preservados (possível reativação)

## Migrations do Banco de Dados

### V6__Add_tenant_id_to_tables.sql

Adiciona a coluna `tenant_id` em todas as tabelas principais:
- users
- roles
- mission_locations
- formative_documents
- document_categories
- formative_stages
- document_reading_progress
- follow_up_meetings

Inclui:
- Foreign keys para a tabela `tenants`
- Índices para otimização de queries

## Fluxo de Dados

### 1. Requisição Chega
```
Cliente → Servidor (Header: X-Tenant-ID)
```

### 2. TenantInterceptor
```
Extrai tenant_id → Valida se está ativo → Define TenantContext
```

### 3. Autenticação (se necessário)
```
JwtAuthenticationFilter → Valida token → Carrega usuário
```

### 4. Processamento da Requisição
```
Controller → Service → Repository
```

### 5. Filtros Hibernate
```
Query automaticamente filtrada por tenant_id
```

### 6. Resposta
```
TenantContext limpo → Resposta enviada ao cliente
```

## Endpoints Públicos (Não requerem X-Tenant-ID)

- `/api/tenants/register` - Registro de novo tenant
- `/api/auth/login` - Login de usuário
- `/api/tenants/subdomain/{subdomain}` - Consulta de tenant por subdomínio
- `/swagger-ui/**` - Documentação Swagger
- `/api-docs/**` - Especificação OpenAPI

## Boas Práticas

### Para Desenvolvimento

1. **Sempre defina X-Tenant-ID no header** das requisições (exceto endpoints públicos)
2. **Nunca faça queries que ignorem o filtro de tenant** sem motivo justificado
3. **Use PlanLimitService** antes de criar recursos
4. **Teste com múltiplos tenants** para garantir isolamento

### Para Produção

1. **Monitore uso de recursos por tenant**
2. **Configure alertas para limites próximos**
3. **Backup regular por tenant**
4. **Auditoria de acessos cross-tenant**

## Segurança

### Isolamento Garantido Por

1. **Filtros Hibernate**: Queries automáticas com WHERE tenant_id = :tenantId
2. **TenantEntityListener**: Previne mudanças de tenant_id
3. **TenantInterceptor**: Valida tenant em toda requisição
4. **AuthController**: Valida tenant no login

### Prevenção de Cross-Tenant Access

- Usuário só pode logar no tenant ao qual pertence
- Todas as queries são filtradas automaticamente
- Tentativas de acesso cross-tenant são logadas e bloqueadas

## Troubleshooting

### Erro: "X-Tenant-ID header is required"
**Solução**: Adicione o header `X-Tenant-ID` com o ID do tenant na requisição

### Erro: "Tenant is suspended or inactive"
**Solução**: Verifique o status do tenant e reative se necessário

### Erro: "User does not belong to the specified tenant"
**Solução**: Use o tenant_id correto no login (mesmo do usuário)

### Erro: "Plan limit exceeded"
**Solução**: Faça upgrade do plano para aumentar os limites

## Próximos Passos

### Melhorias Futuras Sugeridas

1. **Billing Integration**: Integração com Stripe/PayPal
2. **Usage Analytics**: Dashboard de métricas por tenant
3. **Soft Delete**: Retenção de dados após cancelamento
4. **Multi-Database**: Isolamento físico para tenants enterprise
5. **Audit Log**: Registro detalhado de todas as ações
6. **Plan Features**: Features customizadas por plano
7. **Tenant Onboarding**: Wizard de configuração inicial

## Documentação da API

Acesse a documentação completa da API em:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/api-docs

---

**Versão**: 1.0
**Data**: 2025
**Contato**: [Informações de contato do time]
