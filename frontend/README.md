# Gestão Formativa - Frontend

Frontend moderno e responsivo para o sistema de Gestão Formativa, construído com React, TypeScript e Tailwind CSS.

## 🚀 Tecnologias

- **React 18** - Biblioteca JavaScript para construção de interfaces
- **TypeScript** - Superset JavaScript com tipagem estática
- **Vite** - Build tool rápida e moderna
- **Tailwind CSS** - Framework CSS utilitário
- **React Router** - Roteamento declarativo
- **Axios** - Cliente HTTP para consumo de APIs
- **React Icons** - Biblioteca de ícones
- **date-fns** - Manipulação de datas

## 📋 Funcionalidades

### Autenticação
- ✅ Login com JWT
- ✅ Criação de administrador inicial
- ✅ Rotas protegidas
- ✅ Logout

### Gerenciamento de Usuários
- ✅ Listagem de usuários
- ✅ Criação de usuários
- ✅ Edição de usuários
- ✅ Exclusão de usuários
- ✅ Busca e filtros
- ✅ Atribuição de perfis
- ✅ Atribuição de locais de missão
- ✅ Definição de mentores

### Gerenciamento de Perfis
- ✅ Listagem de perfis
- ✅ Criação de perfis
- ✅ Edição de perfis
- ✅ Exclusão de perfis
- ✅ Configuração de permissões

### Locais de Missão
- ✅ Listagem de locais
- ✅ Visualização de detalhes
- ✅ Atribuição de coordenadores

### Etapas Formativas
- ✅ Listagem de etapas ativas
- ✅ Visualização de detalhes
- ✅ Acompanhamento de datas

### Documentos Formativos
- ✅ Listagem de documentos
- ✅ Visualização de documentos
- ✅ Categorização
- ✅ Controle de acesso

### Reuniões de Acompanhamento
- ✅ Listagem de reuniões
- ✅ Filtros (todas, próximas, concluídas)
- ✅ Visualização de detalhes
- ✅ Status das reuniões

### Perfil do Usuário
- ✅ Visualização de informações pessoais
- ✅ Visualização de informações formativas
- ✅ Histórico de etapas

## 🛠️ Instalação

### Pré-requisitos

- Node.js 18+
- npm ou yarn
- Backend rodando (veja instruções no diretório raiz)

### Passos

1. **Navegue até o diretório frontend**
   ```bash
   cd frontend
   ```

2. **Instale as dependências**
   ```bash
   npm install
   ```

3. **Configure as variáveis de ambiente**

   Copie o arquivo `.env.example` para `.env`:
   ```bash
   cp .env.example .env
   ```

   Edite o arquivo `.env` e configure a URL da API:
   ```
   VITE_API_URL=http://localhost:8081/api
   ```

4. **Inicie o servidor de desenvolvimento**
   ```bash
   npm run dev
   ```

5. **Acesse no navegador**
   ```
   http://localhost:5173
   ```

## 📦 Build para Produção

```bash
npm run build
```

Os arquivos otimizados serão gerados no diretório `dist/`.

Para visualizar a build de produção localmente:
```bash
npm run preview
```

## 🔐 Primeiro Acesso

1. **Certifique-se que o backend está rodando**

2. **Acesse a página de inicialização**
   ```
   http://localhost:5173/init-admin
   ```

3. **Crie o administrador inicial**
   - Usuário padrão: `admin`
   - Senha padrão: `admin123`
   - ⚠️ **Importante**: Altere essas credenciais após o primeiro login!

4. **Faça login**
   ```
   http://localhost:5173/login
   ```

## 📁 Estrutura do Projeto

```
frontend/
├── public/              # Arquivos estáticos
├── src/
│   ├── api/            # Serviços de API
│   │   ├── axios.ts           # Configuração do Axios
│   │   ├── authService.ts     # Serviço de autenticação
│   │   ├── userService.ts     # Serviço de usuários
│   │   ├── roleService.ts     # Serviço de perfis
│   │   ├── locationService.ts # Serviço de locais
│   │   ├── stageService.ts    # Serviço de etapas
│   │   ├── documentService.ts # Serviço de documentos
│   │   └── meetingService.ts  # Serviço de reuniões
│   ├── components/     # Componentes reutilizáveis
│   │   ├── common/            # Componentes comuns
│   │   └── layout/            # Componentes de layout
│   ├── contexts/       # Contextos React
│   │   └── AuthContext.tsx    # Contexto de autenticação
│   ├── pages/          # Páginas da aplicação
│   │   ├── auth/              # Páginas de autenticação
│   │   ├── dashboard/         # Dashboard
│   │   ├── users/             # Gerenciamento de usuários
│   │   ├── roles/             # Gerenciamento de perfis
│   │   ├── locations/         # Locais de missão
│   │   ├── stages/            # Etapas formativas
│   │   ├── documents/         # Documentos
│   │   ├── categories/        # Categorias
│   │   ├── meetings/          # Reuniões
│   │   └── profile/           # Perfil do usuário
│   ├── types/          # Definições TypeScript
│   │   └── index.ts           # Tipos e interfaces
│   ├── App.tsx         # Componente principal
│   ├── main.tsx        # Ponto de entrada
│   └── index.css       # Estilos globais (Tailwind)
├── .env.example        # Exemplo de variáveis de ambiente
├── package.json        # Dependências do projeto
├── tailwind.config.js  # Configuração do Tailwind
├── tsconfig.json       # Configuração do TypeScript
└── vite.config.ts      # Configuração do Vite
```

## 🎨 Personalização

### Cores

As cores primárias podem ser personalizadas em `tailwind.config.js`:

```javascript
theme: {
  extend: {
    colors: {
      primary: {
        50: '#f0f9ff',
        // ... outras tonalidades
        900: '#0c4a6e',
      },
    },
  },
}
```

### Estilos Globais

Componentes de estilo reutilizáveis estão definidos em `src/index.css`:
- `.btn`, `.btn-primary`, `.btn-secondary`, etc.
- `.input`, `.label`
- `.card`
- `.table`
- `.badge`

## 🔌 Integração com Backend

O frontend consome a API REST do backend através de serviços organizados em `src/api/`.

### Autenticação

Todas as requisições autenticadas incluem automaticamente o token JWT no header:
```
Authorization: Bearer {token}
```

### Interceptors

O Axios está configurado com interceptors para:
- Adicionar token JWT automaticamente
- Redirecionar para login em caso de 401 (Unauthorized)

## 📱 Responsividade

O aplicativo é totalmente responsivo e otimizado para:
- 📱 Mobile (< 768px)
- 📱 Tablet (768px - 1024px)
- 💻 Desktop (> 1024px)

## 🐛 Solução de Problemas

### Erro de CORS

Se encontrar erros de CORS, verifique:
1. O backend está rodando?
2. A URL da API no `.env` está correta?
3. As configurações de CORS no backend estão habilitadas?

### Token Expirado

O token JWT expira após 24 horas. Se receber erro 401:
1. Faça logout
2. Faça login novamente

### Porta já em uso

Se a porta 5173 já estiver em uso:
```bash
npm run dev -- --port 3000
```

## 📚 Scripts Disponíveis

```bash
# Desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview da build
npm run preview

# Lint
npm run lint
```

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.

## 👥 Autores

Desenvolvido para gerenciamento de comunidades religiosas e formação.

## 📞 Suporte

Para questões e suporte, abra uma issue no repositório do projeto.
