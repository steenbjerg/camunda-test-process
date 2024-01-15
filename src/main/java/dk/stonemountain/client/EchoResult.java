package dk.stonemountain.client;

public class EchoResult {
    public boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "EchoResult [success=" + success + "]";
    }
}
