package android.telecom;

// Compile-only stub. The real class is provided by the phone at runtime.
public abstract class CallScreeningService extends android.app.Service {
    public android.os.IBinder onBind(android.content.Intent intent) { throw new RuntimeException("stub"); }
    public abstract void onScreenCall(Call.Details callDetails);
    public final void respondToCall(Call.Details callDetails, CallResponse response) { throw new RuntimeException("stub"); }

    public static class CallResponse {
        public static class Builder {
            public Builder() { throw new RuntimeException("stub"); }
            public Builder setDisallowCall(boolean b) { throw new RuntimeException("stub"); }
            public Builder setRejectCall(boolean b) { throw new RuntimeException("stub"); }
            public Builder setSilenceCall(boolean b) { throw new RuntimeException("stub"); }
            public Builder setSkipCallLog(boolean b) { throw new RuntimeException("stub"); }
            public Builder setSkipNotification(boolean b) { throw new RuntimeException("stub"); }
            public CallResponse build() { throw new RuntimeException("stub"); }
        }
    }
}
