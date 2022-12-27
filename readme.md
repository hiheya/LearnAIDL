# AIDL

## 一、Android 接口定义语言 (AIDL-Android Interface Definition Language)

### 1.1 概念

- Android 接口定义语言 (AIDL) 与其他接口语言 (IDL) 类似。我们可以利用它定义客户端与服务均认可的编程接口，以便二者使用进程间通信 (IPC) 进行相互通信。在 Android 中，一个进程通常无法访问另一个进程的内存。因此，为进行通信，进程需将其对象分解成可供操作系统理解的原语，并将其编组为可供您操作的对象。编写执行该编组操作的代码较为繁琐，因此 Android 使用了 AIDL 帮助我们处理此问题。

> ⭐注意：只有在需要不同应用的客户端通过 IPC （Inter Process Communication，进程间通信）方式访问服务，并且希望在服务中进行多线程处理时，才有必要使用 AIDL。如果您无需跨不同应用执行并发 IPC，则应通过 实现Binder 来创建接口；或者，如果您想执行 IPC，但不需要处理多线程，课使用 Messenger 来实现接口。

---

### 1.2 具体实现

**使用AIDL 实现 两随机整数的加法运算**

1. 实现远程服务（Service）

- 首先新建一个项目作为service

- 两个进程通信，即需要一个提供服务的service，和一个调用服务的client。

  1.1. 在新建的service项目上创建AIDL File，然后添加一个计算两数之和的方法。

  ![新建AIDL File](https://m.360buyimg.com/babel/jfs/t1/190149/11/31101/288948/63a93769Ea5abbbfd/b1e4f116dc7a1254.png)

    - 代码如下：

  ```java
  // IMyAidlInterface.aidl
  package work.icu007.learnaidl;
  
  // Declare any non-default types here with import statements
  
  interface IMyAidlInterface {
      /**
       * Demonstrates some basic types that you can use as parameters
       * and return values in AIDL.
       */
      void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
              double aDouble, String aString);
      int add(int v1, int v2);
  }
  ```

    - 创建完成之后，rebuild一下项目让Android Studio帮我们生成 IMyAidlInterface.java文件，以便后续实现接口。

  1.2. 创建Service

    - Service中的onBind() 方法 会返回一个IBinder类对象，这个IBinder类对象就是我们 远程服务里面的用来实现具体业务的。为了能够实现具体业务，我们需要写一个内部类MyBinder继承自IMyAidlInterface.Stub，并且实现我们之前添加进去的一个add()方法。

    - 代码如下：

  ```java
  package work.icu007.learnaidl;
  
  import android.app.Service;
  import android.content.Intent;
  import android.os.IBinder;
  import android.os.RemoteException;
  
  public class MyService extends Service {
      public MyService() {
      }
  
      @Override
      public IBinder onBind(Intent intent) {
          return new MyBinder();
      }
  
      class MyBinder extends IMyAidlInterface.Stub{
  
          @Override
          public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
  
          }
  
          @Override
          public int add(int v1, int v2) throws RemoteException {
              return v1 + v2;
          }
      }
  }
  ```

  ---

2. 实现Client调用服务
   2.1. 创建一个和Service 一模一样的AIDL文件，（可以直接从Service端拷贝到Client端）注意包名需一致。

![Client新建 AIDL 文件](https://m.360buyimg.com/babel/jfs/t1/223961/19/15855/276968/63a93c65E65398de7/50a1b3776050d4cd.png)

- 添加完成后和 Service 一样 rebuild一下项目
  2.2. 调用服务

    - 创建ServiceConnection内部类

  为了调用 Service 的服务Client 需要先实现ServiceConnection类来创建服务连接对象，在Client的MainActivity中创建一个内部类AdditionServiceConnection继承自ServiceConnection类，然后重写其onServiceConnected() 绑定成功的回调方法和 onServiceDisconnected() 解绑时的回调方法。该方法接收在Service端定义的MyService的实现作为参数，随后被转换为client端自己的AIDL的实现，通过IMyAidlInterface.Stub.asInterface((IBinder) boundService)来获取iMyAidlInterface实例。

   ```java
   class AdditionServiceConnection implements ServiceConnection{
   
   //        private Service boundService;
   
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
   ```

    - 绑定服务

  通过 Intent 设置service端的Action和Package参数来绑定服务

   ```java
   connection = new AdditionServiceConnection();
   Intent intent = new Intent();
   intent.setPackage("work.icu007.learnaidl");
   intent.setAction("work.icu007.learnaidl.MyService");
   bindService(intent, connection, Context.BIND_AUTO_CREATE);
   ```

- Client端 MainActivity代码如下：

```Java
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
```

- layout.xml代码如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center_horizontal"
    tools:context="work.icu007.learnaidl2.MainActivity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Add by AIDL"
        android:textSize="21sp"
        android:layout_marginTop="30dp"/>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <EditText
            android:id="@+id/v1"
            android:layout_width="100dp"
            android:layout_height="wrap_content"
            android:gravity="center_horizontal">
        </EditText>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="+"
            android:textSize="36sp" />

        <EditText
            android:id="@+id/v2"
            android:layout_width="100dp"
            android:layout_height="wrap_content"
            android:gravity="center_horizontal">
        </EditText>

        <Button
            android:id="@+id/bt_sum"
            android:layout_width="50dp"
            android:layout_height="wrap_content"
            android:hint="=" >
        </Button>

        <TextView
            android:id="@+id/result"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="result"
            android:textSize="21sp" />
    </LinearLayout>

</LinearLayout>
```

### 1.3 注意事项

Android11及以上的版本对应用可见性进行了保护，这两天学习Binder使用AIDL时遇到了客户端调用服务端的方法时报空指针异常，就是找不到那个方法，根本原因是没有发现服务端的服务，导致根本就没有连接上服务端，报错信息如下：

```java
2022-12-26 18:02:26.908 9686-9686/work.icu007.learnaidl2 E/AndroidRuntime: FATAL EXCEPTION: main
    Process: work.icu007.learnaidl2, PID: 9686
    java.lang.NullPointerException: Attempt to invoke interface method 'int work.icu007.learnaidl.IMyAidlInterface.add(int, int)' on a null object reference
        at work.icu007.learnaidl2.MainActivity.onClick(MainActivity.java:36)
        at android.view.View.performClick(View.java:7506)
        at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1119)
        at android.view.View.performClickInternal(View.java:7483)
        at android.view.View.-$$Nest$mperformClickInternal(Unknown Source:0)
        at android.view.View$PerformClick.run(View.java:29335)
        at android.os.Handler.handleCallback(Handler.java:942)
        at android.os.Handler.dispatchMessage(Handler.java:99)
        at android.os.Looper.loopOnce(Looper.java:201)
        at android.os.Looper.loop(Looper.java:288)
        at android.app.ActivityThread.main(ActivityThread.java:7898)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:936)
```

原因就是Android11及以上版本需要在客户端的清单（AndroidManifest.xml）文件里面配置上服务端service的包路径，如下即可：

```xml
<queries>
    <package android:name="work.icu007.learnaidl" />
</queries>
```

