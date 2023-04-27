package com.alumno.simulacionims.gas;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alumno.simulacionims.DataBaseHelper;
import com.alumno.simulacionims.MainActivity;
import com.alumno.simulacionims.R;
import com.alumno.simulacionims.SQLPostgresHelper;
import com.alumno.simulacionims.contrato.ActivityContratoGas;
import com.alumno.simulacionims.contrato.ActivityContratoLuz;
import com.alumno.simulacionims.luz.ActivityLuz_Fecha;
import com.alumno.simulacionims.models.CodigosPrecio;
import com.alumno.simulacionims.models.Simulacion;

import java.util.List;
import java.util.Locale;

/**
 * @author David Ruiz Garcia
 * Objeto ActivityGas para hacer uso de la actividad con dicho nombre
 */
public class ActivityGas extends AppCompatActivity {
    //region Variables
    private EditText cliente;
    private EditText cups;
    private Spinner tarifa;
    private Spinner peaje;
    private Spinner oferta;
    private Button atras;
    private Button siguiente;
    private CheckBox recordar;
    private SharedPreferences prefs;
    private SQLiteDatabase db;
    private Simulacion simula;
    private List<CodigosPrecio> codigos;
    private String tipo;
    CodigosPrecio codigo ;


    private ActivityResultLauncher activityLauncher;

    private final String[] TARI ={"COSTE GESTION FIJO","COSTE GESTION INDEXADO"} ;
    private final String[] PEA ={"RL.1","RL.2","RL.3","RL.4"} ;
    //endregion
    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas);

        cliente = findViewById(R.id.txtClienteGas);
        cups = findViewById(R.id.txtCUPSGas);
        tarifa = findViewById(R.id.spnTarifaGas);
        peaje = findViewById(R.id.spnPeajeGas);
        oferta = findViewById(R.id.spnOfertaGas);
        atras = findViewById(R.id.btnAtrasGas);
        siguiente = findViewById(R.id.btnSiguienteGas);
        recordar = findViewById(R.id.chkRecordar1Gas);

        Bundle extras = getIntent().getExtras();
        tipo = extras.getString("tipo");
        Cargar(prefs);
        simula=recogeValores();
        cups.setText(simula.getCups());

        DataBaseHelper inerbase = new DataBaseHelper(ActivityGas.this, "IMS.db", null, 1);
        db = inerbase.getWritableDatabase();




        activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), null);

        //region spnTarifa
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.spinnergas,TARI);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tarifa.setAdapter(adaptador);
        final String[] tar = {""};
        tarifa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tar[0] = (String) tarifa.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No seleccionaron nada
            }


        });
        //endregion
        //region spnPeaje
        ArrayAdapter<String> adaptado = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.spinnergas,PEA);
        adaptado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        peaje.setAdapter(adaptado);
        String pe = (String) peaje.getSelectedItem();
        peaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String pe = (String) peaje.getSelectedItem();
                System.out.println("tarifa"+tar[0]);
                System.out.println("peaje"+pe);
                SQLPostgresHelper pdb = new SQLPostgresHelper();
                codigos = pdb.getCodigos(pe,tar[0]);
                String [] listofe;

                if(codigos.size() == 0) {
                    listofe = new String[1];
                    listofe[0] = "No hay precios";
                }else{
                    listofe=new String[codigos.size()];
                    for (int i = 0; i < codigos.size(); i++) {

                        listofe[i]= codigos.get(i).getCodigo();
                    }
                }
                ArrayAdapter<String> adap = new ArrayAdapter<String>
                        (getApplicationContext(), R.layout.spinnergas, listofe);
                adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                oferta.setAdapter(adap);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No seleccionaron nada
            }


        });
        //endregion
        //region spnOferta
        oferta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ofe = (String) oferta.getSelectedItem();
                for (int i =0;i<codigos.size();i++){
                    if(codigos.get(i).getCodigo().equals(ofe)){
                        codigo=codigos.get(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No seleccionaron nada
            }


        });
        //endregion
        //region chkRecordar
        recordar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (recordar.isChecked()) {
                    Guardar();
                    Toast.makeText(getApplicationContext(), "Se recordaran los datos insertados", Toast.LENGTH_SHORT).show();
                } else {
                    Vaciar();
                    Guardar();
                    Toast.makeText(getApplicationContext(), "No se guardaran los datos insertados", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //endregion
        //region btnAtras
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anteriorActividad();
            }
        });
        //endregion
        //region btnSiguiente
        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cliente.length() == 0 && cups.length() == 0 || oferta.getSelectedItem().toString().equalsIgnoreCase("No hay precios") ){
                    Toast.makeText(getApplicationContext(), "Tiene que rellenar los campos o debe de haber una OFERTA", Toast.LENGTH_SHORT).show();
                }else{
                    actualizaDB();
                    lanzarActividad_fecha(null);
                }
            }
        });
        //endregion
    }
    //endregion
    //region onBackPress

    /**
     * Mediante este método el usuario puede ir a la actividad anterior
     */
    //TODO Este metodo sirve para volver a la Main activity
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        anteriorActividad();
    }
    //endregion
    //region Guardar_Cargar

    /**
     * Mediante este método se consigue que los datos escritos por el usuario se guarden
     */
    public void Guardar() {
        SharedPreferences preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("cliente", cliente.getText().toString());
        editor.putString("cups", cups.getText().toString());
        editor.putBoolean("Checked", recordar.isChecked());
        editor.commit();
    }

    public void Cargar(SharedPreferences prefs){
        try {
            //Recuperar los valores guardados en SharedPreferences
            prefs = getSharedPreferences("datos", Context.MODE_PRIVATE);
            recordar.setChecked(prefs.getBoolean("Checked", false));
            cliente.setText(prefs.getString("cliente", ""));
            cups.setText(prefs.getString("cups", ""));
        } catch(Exception e){
            Toast.makeText(getApplicationContext(),"Vuelve a escribir los datos a rellenar",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Este metodo vacia los campos de texto de la actividad
     */
    public void Vaciar(){
        cliente.setText("");
        cups.setText("");
    }
    //endregion
    //region ModificaDB

    /**
     * Devuelve el Objeto Simulacion con los datos de el cups que recogeremos de la base de datos interna
     * @return
     */
    @SuppressLint("Range")
    public Simulacion recogeValores() {
        String sentencia;
        Simulacion simu = new Simulacion();
        DataBaseHelper inerbase = new DataBaseHelper(getApplicationContext(), "IMS.db", null, 1);
        db = inerbase.getWritableDatabase();
        sentencia = "SELECT * FROM SIMULACION";
        System.out.println(sentencia);
        Cursor c = db.rawQuery(sentencia, null);

        c.moveToFirst();
        simu.setCups(c.getString(c.getColumnIndex("CUPS")));


        c.close();
        return simu;
    }

    /**
     * Mediante este método se recogen los valores de los spinner como string
     * y todos lo datos de la actividad se almacenan en la base de datos interna
     */
    public void actualizaDB(){
        System.out.println("Tarifa: ");
        System.out.println(tarifa.getSelectedItem().toString());
        System.out.println("Peaje: ");
        System.out.println(peaje.getSelectedItem().toString());
        System.out.println("Oferta: ");
        System.out.println(oferta.getSelectedItem().toString());
        String actualizar= "UPDATE SIMULACION SET CLIENTE = '"+cliente.getText().toString().toUpperCase(Locale.ROOT).trim()+"', CUPS = '"+cups.getText().toString().toUpperCase(Locale.ROOT).trim()+"',TARIFA = '"+tarifa.getSelectedItem().toString()+"',PEAJE = '"+peaje.getSelectedItem().toString()+"',OFERTA = '"+oferta.getSelectedItem().toString()+"',FEE = '"+codigo.getFeecuota()+"',PRECIO_POTENCIA ='"+codigo.getPrpotencia()+"' WHERE ID = 1";
        System.out.println(actualizar);
        db.execSQL(actualizar);
        Toast.makeText(getApplicationContext(),"Los datos fuerón guardados en la base de datos interna",Toast.LENGTH_SHORT).show();
    }
    //endregion
    //region ActividadLanzada

    /**
     * Mediante este método se consigue ir a la siguiente actividad
     * @param view
     */
    public void lanzarActividad_fecha(View view) {
        String peaj = peaje.getSelectedItem().toString();
        String ofer = oferta.getSelectedItem().toString();
        if (tipo.equals("simulacion")) {
            Intent intent = new Intent(getApplicationContext(), ActivityGas_Fecha.class);
            intent.putExtra("peaje", peaj);
            intent.putExtra("oferta", ofer);
            activityLauncher.launch(intent);
        }else if(tipo.equals("contrato")){
            Intent intent = new Intent(getApplicationContext(), ActivityContratoGas.class);
            activityLauncher.launch(intent);
        }
    }

    /**
     * Mediante este método se consigue ir a la anterior actividad
     */
    public void anteriorActividad(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Home");
        builder.setMessage("¿Está seguro de que desea volver al Home?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                activityLauncher.launch(i);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //endregion

}
