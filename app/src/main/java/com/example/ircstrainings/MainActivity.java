package com.example.ircstrainings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView mDisplayDate1;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    EditText etmale;
    EditText etfemale;
    TextView tvtotal;
    CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8;
    Button btn;



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission. After API 24 or so, this needs to be explicit.
        //This will pop up a verification if External storage was not enabled.
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("AYY", "ha2");
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public void generatePDFbuttonclicked(View v) //button clicked to generate PDF.
    {   //This function calls takeScreenshot() to make the PDF.
        // Then it stores PDF in external storage under reports_made folder with the name "report.pdf".


        takeScreenShot(); //makes pdf.


        //VIEW PDF immediately after storing it.
        String getfrom = Environment.getExternalStorageDirectory().getPath() + "/reports_made/";
        File pdfFile = new File(getfrom,"report.pdf");//File path

        if (pdfFile.exists()) //Checking if the file exists or not
        { //if PDF exists, which it totally should
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Do something for Nougat and above versions.
                //In Nougat and above, FileProvider is needed as otherwise it causes a UriExposed exception.
                //This is because android developers decided to tighten security in Nougat and above.

                Intent viewpdf = new Intent(Intent.ACTION_VIEW);
                viewpdf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri apkURI = FileProvider.getUriForFile(
                        MainActivity.this,
                        this.getApplicationContext()
                                .getPackageName() + ".provider", pdfFile);
                viewpdf.setDataAndType(apkURI,"application/pdf"); //search for PDF reader
                viewpdf.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(viewpdf);
                }
                catch (Exception e) {
                    Toast.makeText(MainActivity.this,"No PDF reader available to open report", Toast.LENGTH_SHORT).show();
                }

            } else {
                // do something for phones running an SDK before Nougat
                // This does not support FileProvider, so alternate code
                Uri path = Uri.fromFile(pdfFile);
                Intent objIntent = new Intent(Intent.ACTION_VIEW);
                objIntent.setDataAndType(path, "application/pdf"); //search for PDF READER
                objIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(objIntent);
                }
                catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this,"No PDF reader available to open report", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else { //if PDF does not exist
            Toast.makeText(MainActivity.this, "The file does not exist! ", Toast.LENGTH_SHORT).show();
        }

    }

    private void takeScreenShot() //generates bitmap using getBitmapFromView function, then creates PDF
    {

        ConstraintLayout z = findViewById(R.id.page1);
        int totalHeight = z.getHeight();
        int totalWidth = z.getWidth();
        Bitmap b = getBitmapFromView(z, totalHeight, totalWidth); //returns bitmap of first page.
        //The scrollview layout is such that it houses a constraint layout.
        //That constraint layout is the parent of many constraint layouts, all configured such that one constraint layout equals one page of the pdf.


        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/reports_made/";

        File pdfDir = new File(directory_path);
        if (!pdfDir.exists()) {
            pdfDir.mkdir();
            if (!pdfDir.mkdir())
                Log.i("Directories not made", "Unable to make directories. mkdir() false.");
        }

        String targetPdf = directory_path + "report.pdf";
        File pdfFile = new File(targetPdf);

        try {
            //START MAKING PDF.
            //pdf is made using help of itext.

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            //pdf is saved under reports_made/report.pdf.
            document.open();

            document.add(new Chunk("")); //to avoid no pages error. Creates a page with no content.

            //EVERY PAGE STARTS OFF WITH RED CROSS LOGO.


            //FIRST PAGE CODE
            Bitmap log = BitmapFactory.decodeResource(getResources(), R.drawable.logopiconly);
            Bitmap scaled = Bitmap.createScaledBitmap(log, 50, 50, true);
            String logotarget = directory_path + "logo.jpeg";
            try (FileOutputStream out = new FileOutputStream(logotarget)) {
                scaled.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            Image logo =Image.getInstance(logotarget);
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);

            //Adding the first page
            String targetbitmap = directory_path + "temp.jpeg"; //a temp.jpg is created to temporarily store contents of first page
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); //b contains first page content for now
            } catch (Exception e) {
                e.printStackTrace();
            }
            Image image = Image.getInstance(targetbitmap); //image is now in a format you can add to pdf.
            //now scaling the content for page
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(31,31);
            document.add(image);
            document.newPage();

            //Adding the second page
            z = findViewById(R.id.page2);
            totalHeight = z.getHeight();
            totalWidth = z.getWidth();
            b = getBitmapFromView(z, totalHeight, totalWidth);
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo); //logo added
            image = Image.getInstance(targetbitmap);
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(30,30);
            document.add(image);
            document.newPage();

            //Adding the third page
            z = findViewById(R.id.page3);
            totalHeight = z.getHeight();
            totalWidth = z.getWidth();
            b = getBitmapFromView(z, totalHeight, totalWidth);
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
            image = Image.getInstance(targetbitmap);
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(31,31);
            document.add(image);
            document.newPage();



            //Adding fourth page
            z = findViewById(R.id.page4);
            totalHeight = z.getHeight();
            totalWidth = z.getWidth();
            b = getBitmapFromView(z, totalHeight, totalWidth);
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
            image = Image.getInstance(targetbitmap);
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(31,31);
            document.add(image);
            document.newPage();


            //adding fifth page
            z = findViewById(R.id.page5);
            totalHeight = z.getHeight();
            totalWidth = z.getWidth();
            b = getBitmapFromView(z, totalHeight, totalWidth);
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
            image = Image.getInstance(targetbitmap);
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(31,31);
            document.add(image);
            document.newPage();

            //adding sixth page
            z = findViewById(R.id.page6);
            totalHeight = z.getHeight();
            totalWidth = z.getWidth();
            b = getBitmapFromView(z, totalHeight, totalWidth);
            try (FileOutputStream out = new FileOutputStream(targetbitmap)) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo.setAlignment(Element.ALIGN_RIGHT);
            document.add(logo);
            image = Image.getInstance(targetbitmap);
            image.scaleAbsoluteWidth((document.getPageSize().getWidth()));
            image.scaleAbsoluteHeight((image.getHeight() * document.getPageSize().getWidth() / image.getWidth()));
            image.scalePercent(31,31);
            document.add(image);

            //sixth page which is final page ends with ending logo.
            Bitmap end = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            String targetend = directory_path + "logofull.jpeg";
            try (FileOutputStream out = new FileOutputStream(targetend)) {
                end.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            }
            Image logoend =Image.getInstance(targetend);
            logoend.scalePercent(10);
            logoend.setAlignment(Element.ALIGN_CENTER);
            document.add(logoend);
            document.newPage();


            document.close(); //PDF is made. Now the document is closed for android studio.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) { //gets Bitmap from view.
        //Each constraint view which constitutes a page is saved as a view which is passed on here.
        //This view is converted into a usable bitmap.
        view.setDrawingCacheEnabled(true);
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        Log.i("getBipmapFromView","Bitmap made");
        //iv.setImageBitmap(returnedBitmap);
        return returnedBitmap;
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //as soon as app starts, check if external storage is allowed to be accessed.
        //If it is not, pop up window that asks for storage permission.
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        verifyStoragePermissions(MainActivity.this);

        //Date Picker
        mDisplayDate = (EditText) findViewById(R.id.tvDate);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();                                              //To set the default date as today's date
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));     //To make background transparent
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;              //Because Jan is considered 0, Feb = 1, etc
                Log.d(TAG, "onDateSet: dd/mm/yyyy: " + day + "/" + month + "/" + year);

                String date = day + "/" + month + "/" + year;
                mDisplayDate.setText(date);
            }
        };

        //Date Picker 2
        mDisplayDate1 = (EditText) findViewById(R.id.tvDate1);

        mDisplayDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Calendar cal1 = Calendar.getInstance();                                              //To set the default date as today's date
                int year1 = cal1.get(Calendar.YEAR);
                int month1 = cal1.get(Calendar.MONTH);
                int day1 = cal1.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog1 = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener1,
                        year1,month1,day1);
                dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));     //To make background transparent
                dialog1.show();
            }
        });

        mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker1, int year1, int month1, int day1) {
                month1 = month1 + 1;              //Because Jan is considered 0, Feb = 1, etc
                Log.d(TAG, "onDateSet: dd/mm/yyyy: " + day1 + "/" + month1 + "/" + year1);

                String date1 = day1 + "/" + month1 + "/" + year1;
                mDisplayDate1.setText(date1);
            }
        };

        //Spinner (Drop down list)
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.districts));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        //@Override
        //public void onNothingSelected(AdapterView<?> adapterView) {
        //
        //}

        //Sum of M and F
        etmale = (EditText) findViewById(R.id.etMale);
        etfemale = (EditText) findViewById(R.id.etFemale);
        tvtotal = (TextView) findViewById(R.id.tvTotal);


        etmale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(etmale.getText().toString().equals("")){                 //if else construct to handle null value in the two EditText
                        //try catch block for the int overflow
                    }
                    else if(!etfemale.getText().toString().equals("")){
                        int num_male = Integer.parseInt(etmale.getText().toString());
                        int num_female = Integer.parseInt(etfemale.getText().toString());
                        int num_total = num_male + num_female;
                        String num_totalS = Integer.toString(num_total);
                        tvtotal.setText(num_totalS);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        etfemale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    if (etfemale.getText().toString().equals("")) {

                    } else if (!etmale.getText().toString().equals("")) {
                        int num_male = Integer.parseInt(etmale.getText().toString());
                        int num_female = Integer.parseInt(etfemale.getText().toString());
                        int num_total = num_male + num_female;
                        String num_totalS = Integer.toString(num_total);
                        tvtotal.setText(num_totalS);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            }
        });

        //CheckBoxes

        cb1 = (CheckBox) findViewById(R.id.checkBox1);
        cb2 = (CheckBox) findViewById(R.id.checkBox2);
        cb3 = (CheckBox) findViewById(R.id.checkBox3);
        cb4 = (CheckBox) findViewById(R.id.checkBox4);
        cb5 = (CheckBox) findViewById(R.id.checkBox5);
        cb6 = (CheckBox) findViewById(R.id.checkBox6);
        cb7 = (CheckBox) findViewById(R.id.checkBox7);
        cb8 = (CheckBox) findViewById(R.id.checkBox8);
        btn = (Button) findViewById(R.id.button);

        cb1.setChecked(true);
        cb2.setChecked(true);
        cb3.setChecked(true);
        cb4.setChecked(true);
        cb5.setChecked(true);
        cb6.setChecked(true);
        cb7.setChecked(true);
        cb8.setChecked(true);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cb1.setVisibility(View.VISIBLE);
                cb2.setVisibility(View.VISIBLE);
                cb3.setVisibility(View.VISIBLE);
                cb4.setVisibility(View.VISIBLE);
                cb5.setVisibility(View.VISIBLE);
                cb6.setVisibility(View.VISIBLE);
                cb7.setVisibility(View.VISIBLE);
                cb8.setVisibility(View.VISIBLE);
                cb1.setChecked(true);
                cb2.setChecked(true);
                cb3.setChecked(true);
                cb4.setChecked(true);
                cb5.setChecked(true);
                cb6.setChecked(true);
                cb7.setChecked(true);
                cb8.setChecked(true);
            }
        });


        cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb1.setVisibility(View.GONE);
                }
            }
        });

        cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb2.setVisibility(View.GONE);
                }
            }
        });

        cb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb3.setVisibility(View.GONE);
                }
            }
        });

        cb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb4.setVisibility(View.GONE);
                }
            }
        });

        cb5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb5.setVisibility(View.GONE);
                }
            }
        });

        cb6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb6.setVisibility(View.GONE);
                }
            }
        });

        cb7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb7.setVisibility(View.GONE);
                }
            }
        });

        cb8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                }
                else {
                    cb8.setVisibility(View.GONE);
                }
            }
        });




    }
}