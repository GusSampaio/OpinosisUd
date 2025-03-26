def processar_arquivos_ud(lista_arquivos, arquivo_saida):
    """
    Processa arquivos no formato UD para converter sentenças em uma única linha
    no formato 'token/anotação token/anotação ...' e salva em um arquivo de saída.

    :param lista_arquivos: Lista de caminhos para os arquivos UD.
    :param arquivo_saida: Caminho para o arquivo de saída.
    """
    with open(arquivo_saida, 'w', encoding='utf-8') as saida:
        for arquivo in lista_arquivos:
            with open(arquivo, 'r', encoding='utf-8') as entrada:
                tokens = []
                for linha in entrada:
                    linha = linha.strip()
                    if not linha:  # Sentença completa
                        if tokens:
                            saida.write(" ".join(tokens) + '\n')
                            tokens = []
                    elif not linha.startswith('#'):  # Ignorar comentários
                        partes = linha.split('\t')
                        if len(partes) >= 4:  # Verifica formato UD válido
                            token = partes[1]  # Forme (palavra/token)
                            anotacao = partes[3]  # UPOS (classe gramatical)
                            tokens.append(f"{token}/{anotacao}")
                if tokens:  # Escreve qualquer token restante
                    saida.write(" ".join(tokens) + '\n')

processar_arquivos_ud(["exemplos/example_ud.txt"], "exemplos/tag_ud.txt")