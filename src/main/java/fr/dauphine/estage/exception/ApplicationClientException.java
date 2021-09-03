package fr.dauphine.estage.exception;

public class ApplicationClientException extends Exception {
    private static final long serialVersionUID = 1L;
    private ApplicationClientInterruption applicationInterruption = null;

    public ApplicationClientException(ApplicationClientInterruption interruption) {
        super(interruption.getClientMessage());
        this.setApplicationInterruption(interruption);
    }

    public ApplicationClientInterruption getApplicationInterruption() {
        return this.applicationInterruption;
    }

    public void setApplicationInterruption(ApplicationClientInterruption applicationInterruption) {
        this.applicationInterruption = applicationInterruption;
    }
}
