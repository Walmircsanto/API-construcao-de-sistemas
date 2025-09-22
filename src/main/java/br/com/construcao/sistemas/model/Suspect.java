package br.com.construcao.sistemas.model;


import jakarta.persistence.*;

@Entity
@Table(name = "tb_suspect")
public class Suspect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int age;
    private String urlImage;
    private String cpf;
    private String description;


    public Suspect() {
    }

    public Suspect(Long id, String name, int age, String urlImage, String cpf, String description) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.urlImage = urlImage;
        this.cpf = cpf;
        this.description = description;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Suspect{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", urlImage='" + urlImage + '\'' +
                ", cpf='" + cpf + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
