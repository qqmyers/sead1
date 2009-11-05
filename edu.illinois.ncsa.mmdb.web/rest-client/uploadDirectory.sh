#!/bin/sh
LOCAL_DIRECTORY=$1
PREFIX=$2
# PREFIX is for instance, "http://foo.bar:8080/mmdb"
NS="${PREFIX}/api"

upload_image() {
    curl -s -X POST "${NS}/image" --data-binary @$1
}

upload_directory() {
    COLLECTION_DATA="<ul>";
    for image in $1/*; do
	echo -n uploading $image ... 1>&2
	IMAGE_URL=`upload_image $image`
	if [ $IMAGE_URL ]; then
	    echo " $IMAGE_URL" 1>&2
	else
	    echo " failed" 1>&2
	    exit 1
	fi
	COLLECTION_DATA="${COLLECTION_DATA}<li>${IMAGE_URL}</li>"
    done
    COLLECTION_DATA="${COLLECTION_DATA}</ul>"
    
    echo -n creating collection ... 1>&2
    COLLECTION=`echo $COLLECTION_DATA | curl -s -X POST "${NS}/collection" --data-binary @-`
    echo " $COLLECTION" 1>&2
    echo $COLLECTION
}

upload_directory $LOCAL_DIRECTORY