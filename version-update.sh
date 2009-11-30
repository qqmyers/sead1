FROM="0.2.0.qualifier"
TO="0.3.0.qualifier"
FOLDERS="*"

for f in $FOLDERS; do
  if [ ! -d "$f" ]; then
    continue
  fi

  FILE=$f/META-INF/MANIFEST.MF
  if [ -e "$FILE" ]; then
    sed -i -e "s/Bundle-Version: ${FROM}/Bundle-Version: ${TO}/g" "$FILE"
    if [ "`grep 'Bundle-Vendor:' \"$FILE\"`" = "" ]; then
      echo "Bundle-Vendor: NCSA" >> "$FILE"
    fi
  fi

  FILE=$f/feature.xml
  if [ -e "$FILE" ]; then
    sed -i -e "s/version=\"${FROM}\"/version=\"${TO}\"/g" "$FILE"
  fi

  for g in "$f"/*.product; do
    if [ -e "$g" ]; then
      sed -i -e "s/version=\"${FROM}\"/version=\"${TO}\"/g" "$g"
    fi
  done

done
