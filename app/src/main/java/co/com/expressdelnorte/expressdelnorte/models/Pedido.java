package co.com.expressdelnorte.expressdelnorte.models;


public class Pedido {
    private int id;
    private int tipo;
    private String estado;
    private String direccion;
    private String clienteNombre;
    private String clienteApellidos;
    private String telefono;
    private String celular;
    private String tienda;
    private String direccionTienda;
    private String total;
    private String message_id;

    public Pedido(String total, String direccionTienda, String tienda, String celular, String telefono, String clienteApellidos, String clienteNombre, String direccion) {
        this.total = total;
        this.direccionTienda = direccionTienda;
        this.tienda = tienda;
        this.celular = celular;
        this.telefono = telefono;
        this.clienteApellidos = clienteApellidos;
        this.clienteNombre = clienteNombre;
        this.direccion = direccion;
    }

    public Pedido() {

    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteApellidos() {
        return clienteApellidos;
    }

    public void setClienteApellidos(String clienteApellidos) {
        this.clienteApellidos = clienteApellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public String getDireccionTienda() {
        return direccionTienda;
    }

    public void setDireccionTienda(String direccionTienda) {
        this.direccionTienda = direccionTienda;
    }
    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", direccion='" + direccion + '\'' +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", clienteApellidos='" + clienteApellidos + '\'' +
                ", telefono='" + telefono + '\'' +
                ", celular='" + celular + '\'' +
                ", tienda='" + tienda + '\'' +
                ", direccionTienda='" + direccionTienda + '\'' +
                ", total='" + total + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pedido pedido = (Pedido) o;

        return id == pedido.id && tipo == pedido.tipo;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + tipo;
        return result;
    }
}
