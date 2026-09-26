
# Java Exif Data Renamer

This is a File Renamer written in Java , which read exif data of file like Date and Time and rename file for better sorting!


## Screenshots


![WhatsApp Image 2025-03-19 at 12 11 20_904b2896](https://github.com/user-attachments/assets/9fe5d9e0-fed5-4404-ad7a-8e98b448c6a5)


## Usage
    FolderPath:

    For Android = "/storage/emulated/0/TEST/";
    For Windows = "C:\Users\XYZ\Pictures\Photos";
    
    Format = "yyyy-MM-dd_HH-mm-ss" (by Default)
    

    Command Line Argument:

    java -jar ExifDataRenamer-1.5.jar arg1 arg2
    arg1 - FolderPath (Required)
    arg2 - Format (Optional)



##  Example

    
    java -jar ExifDataRenamer-1.5.jar /storage/emulated/0/TEST/ yyy-MM-dd_HH-mm-ss


## Dependencies

This Project use [metadata-extractor](https://github.com/drewnoakes/metadata-extractor) it is a Java library for reading metadata from media files.
## Authors

- [@manish222261](https://github.com/murmumanish/)

