# Cheatsheet
## Como gerar um arquivo .jar (executável) a partir de arquivos compilados (.class)
``jar cfm app.jar .\META-INF\MANIFEST.MF -C com . -C org .``  

- Este comando cria um arquivo JAR chamado app.jar, incluindo:
    - Um arquivo de manifesto localizado em .\META-INF\MANIFEST.MF.
    - Todos os arquivos e subdiretórios do diretório com e org (mantendo suas estruturas de diretórios no JAR). Estes arquivos devem estar compilados

## Como gerar um arquivo .class a partir de um .java
`` javac MainClass.java `` 
- Isso irá gerar um arquivo .class chamado MainClass.class no mesmo diretório onde está o arquivo fonte .java.
- Se o seu código depende de outras classes ou bibliotecas que estão em diferentes pacotes ou diretórios, você deve compilar usando a flag -classpath para garantir que o compilador encontre todas as dependências necessárias.
    - Por exemplo, suponha que o arquivo ``MainClass.java`` está no pacote ``com.example``. Se esse código utiliza classes que estão fora dessa pasta, você deve garantir que o compilador saiba onde encontrá-las. Nesse caso, saia da pasta ``com`` e compile o arquivo com o comando:
    `` javac -classpath . com/example/MainClass.java`` 
    - O . (ponto) indica que o compilador deve procurar as classes no diretório atual e subdiretórios.


## Como gerar um arquivo .java a partir de um .class
O jeito mais simples é instalar o Java Decompiler, abrir o arquivo .class desejado, criar um arquivo .java vazio e copiar o que está no Decompiler neste arquivo .java

## Como gerar os arquivos .class associados a um arquivo .jar
`` jar xf nome_do_arquivo.jar ``
- Depois de executar este comando, os arquivos .class (e outros, como arquivos de manifesto ou recursos) estarão disponíveis no diretório de trabalho ou nos subdiretórios, conforme a estrutura original dentro do .jar.

## Como combinar o uso do compilador Java (javac) com o PowerShell para compilar arquivos Java de forma recursiva a partir de vários diretórios
`` javac -d out -cp org (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)  `` 
- O comando completo está buscando recursivamente todos os arquivos .java a partir do diretório atual (ou subdiretórios) e depois compila todos esses arquivos com o javac. O compilador coloca os arquivos .class no diretório out e usa o diretório org como classpath.