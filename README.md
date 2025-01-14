# AZFSHelper

`AZFSHelper` é uma classe utilitária para interagir com o serviço **Azure File Share** da Microsoft, oferecendo métodos para inicializar a conexão com o Azure, fazer upload, download, listar, mover e deletar arquivos em um compartilhamento de arquivos. A classe utiliza a SDK do Azure Storage para interagir com o serviço.

## Pré-requisitos

- Java 17 ou superior.
- Maven para gerenciamento de dependências.
- Uma conta do Azure com o serviço de **Azure File Share** configurado.
- Dependências no **pom.xml** do projeto, incluindo o SDK do Azure e SLF4J para logging.

## Dependências

Adicione as dependências abaixo ao seu `pom.xml`:

```xml
<dependencies>
    <!-- Azure Storage SDK -->
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-storage-file-share</artifactId>
    </dependency>

    <!-- SLF4J API -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.9</version>
    </dependency>

    <!-- Log4j SLF4J Bridge -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j2-impl</artifactId>
        <version>2.20.0</version>
    </dependency>
</dependencies>
```

## Instalação
Clone ou baixe o repositório.
Adicione as dependências necessárias no seu projeto Maven.
Configure as credenciais do Azure (connection string, share name e account name).

Como Usar:
### 1. Instanciação da Classe
Para usar o AZFSHelper, você precisa passar a string de conexão, o nome do compartilhamento de arquivos e o nome da conta do Azure ao criar uma instância da classe.
Para mais detalhes, visite o [link oficial da documentação do Azure Storage File Share](https://learn.microsoft.com/en-us/java/api/overview/azure/storage-file-share-readme?view=azure-java-stable).


```java
AZFSHelper helper = new AZFSHelper("<connectionString>", "<shareName>", "<accountName>");
```
## 2.Métodos Disponíveis
Este método inicializa a conexão com o Azure File Share. Ele usa a string de conexão, nome do compartilhamento e nome da conta para se conectar.
```java
initConnection();
```

Faz o download de um arquivo específico do Azure File Share e retorna um OutputStream.
```java
donwloadFileAsStream(String fileName, String filePath);
```

Lista arquivos em um diretório específico, com a opção de buscar arquivos que correspondem exatamente ao nome ou que contenham uma substring.
```java
listFiles(String matchFileName, String filePath, boolean matchExactly);
```

Faz o upload de um arquivo para um diretório específico no Azure File Share.
```java
uploadFile(String fName, StorageFileInputStream file, String filePath);
```

Deleta um arquivo do Azure File Share.
```java
deleteFile(String fileName, String filePath);
```

Move um arquivo de um diretório para outro, com a opção de deletar o arquivo original após a transferência.
```java
moveFile(String fileName, String fromFilePath, String toFilePath, boolean deleteOriginal);
```