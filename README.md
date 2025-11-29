### Repositorio com workflow ativo, apos cada commit a aplicação automaticamente atualiza e reinicia o servico
- GitHub Workflow

### Pipeline build back via VM

- Clonar repositorio: git clone https://github.com/lab-dev-core/gerenciamento-organizacional-frontend.git
- Acessar a pasta do repositorio e subir os dockers do bd: docker-compose up -d
- mvn clean install
- mvn clean package
- entrar na pasta target: cd target/
- rodar: java -jar gestaoFormativa-0.0.1-SNAPSHOT.jar --server.port=8081

  ## Criar serviço para rodar o back end em background
  
- sudo nano /etc/systemd/system/gestaoformativa.service
## conteudo do file (a ser disponibilizado na pasta misc)
[Unit]
Description=Gestão Formativa App
After=network.target

[Service]
ExecStart=/usr/bin/java -jar /home/dev-core/htdocs/app.dev-core.online/gerenciamento-organizacional-backend/target/gestaoFormativa-0.0.1-SNAPSHOT.jar --server.port=8081
Restart=always
User=seuusuario

[Install]
WantedBy=multi-user.target
## end file

-  sudo systemctl enable gestaoformativa
