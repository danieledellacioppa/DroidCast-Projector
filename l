#!/bin/bash

# Verifica se JAVA_HOME è impostato
if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME non è impostato. Eseguo l'export..."
  export JAVA_HOME="/home/daniele/android-studio/jbr"
  export PATH="$PATH:$JAVA_HOME/bin"
fi

# Esegui la build
./gradlew assembleDebug

# Verifica il codice di uscita della build
build_exit_code=$?

if [ $build_exit_code -eq 0 ]; then
  echo "Build completata con successo!"

  # Prova ad installare l'app
  adb -s ZY32HW49KC install -r app/build/outputs/apk/debug/app-debug.apk

  # Verifica il codice di uscita dell'installazione
  install_exit_code=$?

  if [ $install_exit_code -ne 0 ]; then
    echo "Installazione fallita, possibile problema di firma non corrispondente. Tentativo di reinstallazione..."
    adb -s ZY32HW49KC uninstall com.forteur.droidcast_projector
    adb -s ZY32HW49KC install -r app/build/outputs/apk/debug/app-debug.apk
  fi

  # Controlla se la variabile DEBUG è impostata su "true"
  if [ "$DEBUG" = "true" ]; then
    echo "Esecuzione in modalità debug..."
    adb -s ZY32HW49KC shell am start -D -n  com.forteur.droidcast_projector/.MainActivity
  fi
else
  echo "La build ha avuto errori. L'installazione non verrà effettuata..."
fi

