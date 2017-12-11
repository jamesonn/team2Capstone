package Firebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTimeComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import team2.mkesocial.Constants;
import team2.mkesocial.R;

/**
 * Created by cfoxj2 on 12/8/2017.
 */

public class MethodOrphanage {

    public static String getFullAddress(String location){
        String fullAddress;//0000 Street Name, City, State Zip, Country:LatLng:(0,0)
        //City, State Zip, Country:LatLng:(0,0)
        //State Zip, Country:LatLng:(0,0)
        fullAddress = location.substring(0, location.indexOf(":"));
        String[] addr = fullAddress.split(",");
        //for loop to append the stuff together and then return it
        String firstPart = " ";
        for(int i=0;i<addr.length;++i){
            firstPart+=addr[i]+"\n";
        }
        return firstPart;
    }

    public static Double getLat(String location){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[0]);
    }

    public static Double getLng(String location){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[1]);
    }

    /**
     * -1 if date1 is before date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDates(GregorianCalendar date1, GregorianCalendar date2)
    {
        if(date1 == null || date2 == null) return 0;
        //get rid of time
        DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
        return comparator.compare(date1, date2);


    }
    /**
     * -1 if time1 is before time2
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int compareTimes(GregorianCalendar time1, GregorianCalendar time2)
    {
        if(time1 == null || time2 == null) return 0;
        //get rid of date part
        DateTimeComparator comparator = DateTimeComparator.getTimeOnlyInstance();
        return comparator.compare(time1, time2);

    }


    /*************************************
     * IMAGE UPLOADING Helper Methods:
     * setting up image picker
     * retrieving image
     * uploading (store new, delete old)
     * ************************************/
    //get file extension information from URI info on picture
    private static String getFileExtension(Activity a, Uri uri) {
        ContentResolver cR = a.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**
     * Uploads new file to storeRef, deleting old if it's different,
     *  and returns URI of image's firestore location
     * @param a
     * @param storageReference
     * @param newFilePath
     * @param oldFilePath
     * @return
     */
    public static void uploadFile(Activity a, StorageReference storageReference, Uri newFilePath, String oldFilePath, DatabaseReference placeToStoreRef) {
        //checking if file is available
        if (newFilePath != null && !newFilePath.toString().equals(oldFilePath)) {

            //getting the storage reference
            StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS
                    + System.currentTimeMillis() + "." + getFileExtension(a, newFilePath));

            //adding the file to reference
            sRef.putFile(newFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //displaying success toast
                            Toast.makeText(a.getApplicationContext(), "Image Uploaded ", Toast.LENGTH_LONG).show();
                            placeToStoreRef.setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(a.getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });

            //Delete old image
            if (oldFilePath != null && !oldFilePath.isEmpty()){
                StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldFilePath);
                oldRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        Log.e("firebasestorage", "onSuccess: deleted file");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Log.e("firebasestorage", "onFailure: did not delete file");
                    }
                });
            }
        }
    }

    //fix rotation issues
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static Bitmap onPictureResult(Intent data, int resultCode, Activity a, ImageView eventImage, ContentResolver cr, Uri file) {
        Bitmap adjustedBitmap = null;
        if (data != null && data.getData() != null) {
            file = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, file);
                //For correcting orientation so it displays correctly (on images that where taken sideways/upside-down)
                ExifInterface exif = new ExifInterface(cr.openInputStream(file));
                //get current rotation...
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                //convert to degrees
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                }
                adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
            }
        }
        /**Uri filePath = null;
         // Check if an image was selected
         if (data != null) {
         filePath = data.getData();
         if (resultCode == -1) {//RESULT_OK = -1
         Uri selectedMediaUri = data.getData();
         if (selectedMediaUri.toString().contains("image")) {
         try {//Update the display values
         Bitmap bitmap = MediaStore.Images.Media.getBitmap(a.getContentResolver(), filePath);
         //For correcting orientation so it displays correctly (on images that where taken sideways/upside-down)
         Matrix matrix = new Matrix();
         try {
         ExifInterface exif = new ExifInterface(a.getContentResolver().openInputStream(filePath));
         //get current rotation...
         int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
         //convert to degrees
         int rotationInDegrees = exifToDegrees(rotation);

         if (rotation != 0f) {
         matrix.preRotate(rotationInDegrees);
         }
         }catch(IOException e){}


         // Screen height
         DisplayMetrics display = new DisplayMetrics();
         a.getWindowManager().getDefaultDisplay().getMetrics(display);
         int screenWidth = display.widthPixels;
         int screenHeight = display.heightPixels;

         Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
         Bitmap scaledBitmap = resize(adjustedBitmap, bitmap.getWidth(), screenHeight / 3);
         //resize the imageView displaying image
         android.view.ViewGroup.LayoutParams layoutParams = eventImage.getLayoutParams();
         layoutParams.width = eventImage.getWidth();
         layoutParams.height = scaledBitmap.getHeight();
         eventImage.setLayoutParams(layoutParams);

         eventImage.setImageBitmap(scaledBitmap);


         } catch (Exception e) {
         }
         } else {
         Toast.makeText(a.getApplicationContext(), "Incorrect Image format selected", Toast.LENGTH_LONG).show();
         }
         }
         }
         return filePath;*/
        return adjustedBitmap;
    }

    public static String convertToDBFormat(List<String> attendeeList)
    {
        String attendees = "";
        if(attendeeList == null || attendeeList.isEmpty()) return attendees;
        for(String a: attendeeList)
        {
            attendees += a +"`";
        }
        return attendees.substring(0, attendees.length() - 1);
    }

    public static void updateUserHosting(DatabaseReference userDatabase, String userId, String hostListString,
                                         String eventDelId, String eventDelTitle)
    {
        //Remove event from user's DB profile
        userDatabase.child(userId).child("hostEid").setValue(hostListString
                .replace(eventDelId + "`" + eventDelTitle + "`", ""));

    }


}
