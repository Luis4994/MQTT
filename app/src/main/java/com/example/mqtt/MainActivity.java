package com.example.mqtt;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MainActivity extends AppCompatActivity {
    String clienteID = "";
    //Variables para la conexion al servidor MQTT
    static String MQTTHOST  = "tcp://crudfirebase.cloud.shiftr.oi:1883";
    static String MQTTUSER = "crudfirebase";
    static String MQTTPASS = "dRaf5Qc6cRNwJrZt";
    static String TOPIC = "LED";
    static String TOPIC_MSG_ON = "Encender";
    static  String TOPIC_MSG_OFF = "Apagar";

    //Otras variables de la actividad
    MqttAndroidClient cliente;
    MqttConnectOptions opciones;
    Boolean permisoPublicar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configuracion de la interfaz de usuario
        connectBroker();

        //Eventos de click en botones
        Button btn_encendido = findViewById(R.id.btn_encender);
        btn_encendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(TOPIC, TOPIC_MSG_ON);
            }
        });
        Button btn_apagado = findViewById(R.id.btn_apagar);
        btn_apagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(TOPIC, TOPIC_MSG_OFF);
            }
        });
    }
    //Envia un mensaje al servidor MQTT
    private void enviarMensaje(String topic, String msg){
        checkConnection();
        if (this.permisoPublicar){
            try {
                int qos = 0;
                this.cliente.publish(topic, msg.getBytes(), qos, false);
                Toast.makeText(getBaseContext(), topic + " : " + msg, Toast.LENGTH_SHORT).show();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    //Verfica si la conexion con el servidor MQTT esta activa
    private void checkConnection(){
        if (this.cliente.isConnected()) {
            this.permisoPublicar = true;
        }else{
            this.permisoPublicar = false;
            connectBroker();
        }
    }
    //Establece la conexion con el servidor MQTT
    private void connectBroker(){
        this.cliente = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, this.clienteID);
        this.opciones = new MqttConnectOptions();
        this.opciones.setUserName(MQTTUSER);
        this.opciones.setPassword(MQTTPASS.toCharArray());
        try {
            IMqttToken token = this.cliente.connect(opciones);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getBaseContext(), "Conectado", Toast.LENGTH_SHORT).show();
                    mensajearTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(), "Conexion Fallida", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (MqttException e){
            e.printStackTrace();
        }
    }
    //Suscribe al cliente MQTT al tema y maneja los mensajes recibidos
    private void mensajearTopic(){
        try {
            this.cliente.subscribe(TOPIC,0);
        }catch (MqttSecurityException e){
            e.printStackTrace();
        }catch (MqttException e){
            e.printStackTrace();
        }
        this.cliente.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), "Se desconecto el servidor", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Manejo de los mensajes recibidos
                TextView txtinfo = findViewById(R.id.txtInfo);

                if (topic.matches(TOPIC)){
                    String msg = new String(message.getPayload());
                    if (msg.matches(TOPIC_MSG_ON)){
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(GREEN);
                    }
                    if (msg.matches(TOPIC_MSG_OFF)){
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(RED);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}