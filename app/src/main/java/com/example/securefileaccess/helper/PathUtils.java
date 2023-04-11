package com.example.securefileaccess.helper;
//

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.loader.content.CursorLoader;

public class PathUtils {

    public static String getPath(Context context, Uri fileUri) {
        String realPath;
// SDK < API11
        try {
            if (Build.VERSION.SDK_INT < 11)
                realPath = getRealPathFromURI_BelowAPI11(context, fileUri);
// SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = getRealPathFromURI_API11to18(context, fileUri);
// SDK > 19 (Android 4.4)
            else
                realPath = getRealPathFromURI_API19(context, fileUri);
            return realPath;
        } catch (Exception e) {
// Toast.makeText(context, "Exception in getRealPath: " + e, Toast.LENGTH_SHORT).show();
            throw e;
        }
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media._ID};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null,
                null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media._ID};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String result = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            if (cursor != null) {
                cursor.close();
            }
            return result;
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromURI_API19(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                String fileName = getFilePath(context, uri);
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                }
                try {
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();
                return getDataColumn(context, uri, null, null);
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            try {
                return uri.getPath();
            } catch (Exception e) {
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor == null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getFilePath(Context context, Uri uri) {

        Cursor cursor = null;
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                if (cursor != null) {
                    cursor.close();
                }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}


//import android.annotation.SuppressLint;
//import android.content.ContentUris;
//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.DocumentsContract;
//import android.provider.MediaStore;
//
//import java.net.URISyntaxException;
//
///**
// * Created by Aki on 1/7/2017.
// */
//
//public class PathUtils {
//    /*
//     * Gets the file path of the given Uri.
//     */
//    @SuppressLint("NewApi")
//    public static String getPath(Context context, Uri uri)  {
//        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
//        String selection = null;
//        String[] selectionArgs = null;
//        // Uri is different in versions after KITKAT (Android 4.4), we need to
//        // deal with different Uris.
//        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                return Environment.getExternalStorageDirectory() + "/" + split[1];
//            } else if (isDownloadsDocument(uri)) {
//                final String id = DocumentsContract.getDocumentId(uri);
//                uri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//            } else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//                if ("image".equals(type)) {
//                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//                selection = "_id=?";
//                selectionArgs = new String[]{ split[1] };
//            }
//        }
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] projection = { MediaStore.Images.Media.DATA };
//            Cursor cursor = null;
//            try {
//                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                if (cursor.moveToFirst()) {
//                    return cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//        return null;
//    }
//
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    public static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    public static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    public static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }
//}
////
////import android.content.ContentUris;
////import android.content.Context;
////import android.database.Cursor;
////import android.net.Uri;
////import android.os.Build;
////import android.os.Environment;
////import android.provider.DocumentsContract;
////import android.provider.MediaStore;
////
////public class PathUtils {
////
////    public static String getPath(final Context context, final Uri uri) {
////
////        System.out.println(uri.getPath());
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
////
////            if (isExternalStorageDocument(uri)) {
////                final String docId = DocumentsContract.getDocumentId(uri);
////                final String[] split = docId.split(":");
////                final String type = split[0];
////                String storageDefinition;
////
////
////                if ("primary".equalsIgnoreCase(type)) {
////
////                    return Environment.getExternalStorageDirectory() + "/" + split[1];
////
////                } else {
////
////                    if (Environment.isExternalStorageRemovable()) {
////                        storageDefinition = "EXTERNAL_STORAGE";
////                    } else {
////                        storageDefinition = "SECONDARY_STORAGE";
////                    }
////
////                    return System.getenv(storageDefinition) + "/" + split[1];
////                }
////
////            } else if (isDownloadsDocument(uri)) {
////
////                final String id = DocumentsContract.getDocumentId(uri);
////                final Uri contentUri = ContentUris.withAppendedId(
////                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
////
////                return getDataColumn(context, contentUri, null, null);
////
////            } else if (isMediaDocument(uri)) {
////                final String docId = DocumentsContract.getDocumentId(uri);
////                final String[] split = docId.split(":");
////                final String type = split[0];
////
////                Uri contentUri = null;
////                if ("image".equals(type)) {
////                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
////                } else if ("video".equals(type)) {
////                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
////                } else if ("audio".equals(type)) {
////                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
////                }
////
////                final String selection = "_id=?";
////                final String[] selectionArgs = new String[]{
////                        split[1]
////                };
////
////                return getDataColumn(context, contentUri, selection, selectionArgs);
////            }
////
////        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
////
////            if (isGooglePhotosUri(uri))
////                return uri.getLastPathSegment();
////
////            return getDataColumn(context, uri, null, null);
////
////        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
////            return uri.getPath();
////        }
////
////        return null;
////    }
////
////    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
////
////        Cursor cursor = null;
////        final String column = "_data";
////        final String[] projection = {
////                column
////        };
////
////        try {
////            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
////            if (cursor != null && cursor.moveToFirst()) {
////                final int column_index = cursor.getColumnIndexOrThrow(column);
////                return cursor.getString(column_index);
////            }
////        } finally {
////            if (cursor != null)
////                cursor.close();
////        }
////        return null;
////    }
////
////
////    public static boolean isExternalStorageDocument(Uri uri) {
////        return "com.android.externalstorage.documents".equals(uri.getAuthority());
////    }
////
////
////    public static boolean isDownloadsDocument(Uri uri) {
////        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
////    }
////
////    public static boolean isMediaDocument(Uri uri) {
////        return "com.android.providers.media.documents".equals(uri.getAuthority());
////    }
////
////    public static boolean isGooglePhotosUri(Uri uri) {
////        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
////    }
////}