package com.myow.model;

public class ItemEstoque {
    private String id;
    private String nome;
    private String lote;
    private String categoria;
    private int quantidade;
    private int estoqueMinimo;
    private String validade;
    private double valorUnitario;
    private String unidade;

    public ItemEstoque() {}

    public ItemEstoque(String id, String nome, String lote, String categoria, int quantidade, int estoqueMinimo, String validade, double valorUnitario, String unidade) {
        this.id = id;
        this.nome = nome;
        this.lote = lote;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.validade = validade;
        this.valorUnitario = valorUnitario;
        this.unidade = unidade;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public int getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public boolean isEstoqueBaixo() {
        return this.quantidade <= this.estoqueMinimo;
    }

    @Override
    public String toString() {
        return nome + " (Lote: " + lote + ") - Saldo: " + quantidade + " " + unidade;
    }
}
