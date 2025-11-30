#!/bin/bash

# Компиляция
./compile.sh

if [ $? -eq 0 ]; then
    # Запуск
    echo ""
    ./run.sh
fi
