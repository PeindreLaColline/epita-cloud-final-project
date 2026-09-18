import os
import boto3

TABLE_ARNS = os.environ["TABLE_ARNS"].split(",")
BACKUP_BUCKET = os.environ["BACKUP_BUCKET"]

dynamodb = boto3.client("dynamodb")


def handler(event, context):
    exports = []
    for table_arn in TABLE_ARNS:
        table_name = table_arn.split("/")[-1]
        response = dynamodb.export_table_to_point_in_time(
            TableArn=table_arn,
            S3Bucket=BACKUP_BUCKET,
            S3Prefix=f"{table_name}/",
            ExportFormat="DYNAMODB_JSON",
        )
        exports.append(response["ExportDescription"]["ExportArn"])
    return {"exports": exports}
