import re

def limpar_texto_humano_extrativo(texto):
    # Remove tags do tipo <...> no final de cada linha
    linhas = texto.splitlines()
    linhas_limpa = [re.sub(r'\s*<[^>]+>\s*$', '', linha) for linha in linhas]
    return '\n'.join(linhas_limpa)

texto = """
A história em si é interessante. <D67_S7>
Contém medo e suspense desde a primeira linha. <D80_S4>
Em vários pontos chega a ser um relato de a realidade vivida em os dias atuais, em menor ou maior grau, em o mundo todo. <D67_S9>
A discussão sobre o controle de o governo sob o livre abítrio é ímpar e merece uma atenção especial. <D141_S5>
O livro talvez desanime leitores mais afoito em suas primeiras páginas. <D141_S2>
A história não tem pressa para acontecer e a ação só começa mesmo de a metade para o final.
Bom livro. <D67_S10>
Vale a leitura e vale a re-leitura. <D189_S9>
"""

print(limpar_texto_humano_extrativo(texto))