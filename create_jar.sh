#!/bin/bash

echo "Создание JAR файла..."

# Сначала компилируем проект
./compile.sh

if [ $? -ne 0 ]; then
    exit 1
fi

# Создаем временную папку для JAR
mkdir -p jar_build

# Копируем скомпилированные классы
cp -r out/* jar_build/

# Копируем config.properties
cp config.properties jar_build/

# Создаем манифест
echo "Main-Class: App" > jar_build/manifest.txt
echo "Class-Path: ." >> jar_build/manifest.txt

# Создаем JAR
cd jar_build
jar cfm ../zooapp.jar manifest.txt .

# Очищаем временные файлы
cd ..
rm -rf jar_build

echo "✓ JAR файл создан: zooapp.jar"
echo "Запуск: java -jar zooapp.jar"