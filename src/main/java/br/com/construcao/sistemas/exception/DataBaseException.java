package br.com.construcao.sistemas.exception;

public class DataBaseException extends RuntimeException{
    public DataBaseException(String msm){
        super(msm);
    }
}
