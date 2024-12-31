# Youtrack db exporter

A simple script for exporting from [Youtrack](https://www.jetbrains.com/youtrack/) database dump
to CSV files, data that is not present in Youtrack's CSV export:
* Links between issues
* Issue comments

## Usage

Run the following command:
```bash
gradlew run --args='"<youtrack_db_dir>" "<export_dir>"'
```
Where:  
`<youtrack_db_dir>` - path to "youtrack" directory in the unpacked database dump.  
`<export_dir>` - path to the directory CSV file will be written to.
