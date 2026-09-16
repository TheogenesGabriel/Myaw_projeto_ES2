package com.myow.model;

public class ItemUtilizado {
    private String itemId;
    private String nome;
    private int quantidade;
    private double valorUnitario;

    public ItemUtilizado() {}

    public ItemUtilizado(String itemId, String nome, int quantidade, double valorUnitario) {
        this.itemId = itemId;
        this.nome = nome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public double getSubtotal() {
        return quantidade * valorUnitario;
    }
}
