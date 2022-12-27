package work.icu007.learnaidl2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import work.icu007.learnaidl.IMyAidlInterface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    AdditionServiceConnection connection;
    private IMyAidlInterface iMyAidlInterface;
    private TextView result;
    private EditText value1;
    private EditText value2;
    int v1,v2,res = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_sum:
                result = findViewById(R.id.result);
                value1 = findViewById(R.id.v1);
                value2 = findViewById(R.id.v2);
                v1 = Integer.parseInt(value1.getText().toString());
                v2 = Integer.parseInt(value2.getText().toString());
                try {
                    res = iMyAidlInterface.add(v1, v2);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                result.setText(res + "");
                break;
        }
    }

    class AdditionServiceConnection implements ServiceConnection{

//        private Service boundService;

        // 服务绑定成功时的回调方法该方法接收在service端定义的MyService的实现作为参数，随后被转换为client端自己的AIDL的实现
        @Override
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface((IBinder) boundService);
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iMyAidlInterface = null;
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_sum).setOnClickListener(this);
        connection = new AdditionServiceConnection();
        Intent intent = new Intent();
        intent.setPackage("work.icu007.learnaidl");
        intent.setAction("work.icu007.learnaidl.MyService");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        connection = null;
    }
}