# CS-Proyecto — Servicio seguro de contenidos multimedia (Fase I)

Proyecto de la asignatura **Compresión y Seguridad (2026-2027)**.
Aplicación de escritorio en Java que **cifra archivos multimedia con AES-128**, protege la clave de cada archivo con **RSA-2048** y protege la clave privada RSA con una **contraseña** del usuario.

[Descargar JDK 26 para Windows](https://www.oracle.com/es/java/technologies/downloads/#jdk26-windows)

---

## Índice

1. [Qué hace](#qué-hace)
2. [Requisitos y ejecución](#requisitos-y-ejecución)
3. [Manual de uso](#manual-de-uso)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Descripción de cada clase](#descripción-de-cada-clase)
6. [Formato de `database.csv`](#formato-de-databasecsv)
7. [Diseño criptográfico](#diseño-criptográfico)
8. [Limitaciones conocidas y trabajo futuro](#limitaciones-conocidas-y-trabajo-futuro)

---

## Qué hace

En esta **Fase I hay un único usuario (`u`)**. El programa permite:

- **Cifrar** archivos (imágenes, audio, vídeo, cualquier archivo) seleccionándolos con el explorador o **arrastrándolos** a la ventana.
- **Descifrar** los archivos ya cifrados y guardarlos en una carpeta local; el archivo se **abre automáticamente** al terminar.

Cada archivo se cifra con una **clave AES distinta generada aleatoriamente**. Esa clave nunca se guarda en claro: se cifra con la clave pública RSA del usuario.

## Requisitos y ejecución

- **JDK 17 o superior** (probado con JDK 21; el enlace de arriba descarga el JDK 26). Se necesita SHA3-256, disponible desde Java 9.
- **Windows**: doble clic en `Iniciar.bat` (compila y abre la aplicación sin consola).
- `Iniciar(ConConsola).bat` hace lo mismo pero mantiene la consola abierta, útil para ver errores.

Equivalente manual, desde la raíz del proyecto:

```bat
javac src\code\*.java src\code\cypher\*.java
java -cp src\code Main
```

> **Importante:** el programa debe ejecutarse **desde la raíz del proyecto** (donde está `Iniciar.bat`), porque las rutas `src/database`, `cifrados` y `descifrados` son relativas a ella.

## Manual de uso

1. **Primer arranque:** aparece un diálogo para **crear la contraseña** del usuario. Con ella se generan las claves RSA y se protege la clave privada. **No hay forma de recuperarla**: si se pierde, los archivos cifrados no se pueden descifrar.
2. **Siguientes arranques:** el diálogo pide esa misma contraseña. Si es incorrecta, se vuelve a pedir; si se cancela, el programa se cierra.
3. **Cifrar:**
   - Botón **Cifrar** → se abre el explorador (se pueden elegir varios archivos), o
   - **arrastra** uno o varios archivos sobre la zona de texto.
   - El resultado de cada archivo aparece en pantalla. El archivo cifrado queda en la carpeta **`cifrados/`** (`nombre.ext.enc`) y también en la base de datos.
4. **Descifrar:** botón **Descifrar** → elige uno o varios archivos de la lista → se guardan en **`descifrados/`** con su nombre original y **se abren** con el programa predeterminado.

Reglas y avisos:

- No pueden existir dos archivos con el mismo nombre; el segundo se rechaza con un aviso.
- Los nombres no pueden contener `;` (es el separador del CSV).
- Las carpetas no se pueden cifrar, solo archivos.

## Estructura del proyecto

```
.
├── .gitignore
├── Iniciar.bat                  Compila y ejecuta sin consola
├── Iniciar(ConConsola).bat      Compila y ejecuta mostrando la consola
├── README.md
├── cifrados/                    (se crea sola) archivos cifrados .enc
├── descifrados/                 (se crea sola) archivos recuperados en claro
└── src/
    ├── code/
    │   ├── Main.java            Lógica principal: cifrado/descifrado, usuario, sesión
    │   ├── UI.java              Interfaz gráfica (Swing)
    │   ├── DbManager.java       Lectura/escritura de database.csv
    │   ├── PasswordManager.java Hash SHA3-256 + salt de la contraseña
    │   ├── RandKeyGenerator.java Generador de bytes aleatorios seguros
    │   └── cypher/
    │       ├── AES.java         Cifrado simétrico AES-128-GCM
    │       └── RSA.java         Cifrado asimétrico RSA-2048
    ├── database/
    │   └── database.csv         "Base de datos": usuario y archivos cifrados
    └── img/                     Imágenes de prueba (solo usadas por las funciones de depuración)
```

`cifrados/` y `descifrados/` están en `.gitignore`: el contenido en claro no debe subirse al repositorio.

## Descripción de cada clase

| Clase | Responsabilidad |
|-------|-----------------|
| `Main` | Punto de entrada y **lógica de negocio**: registro/login, cifrar y descifrar archivos, guardado en `cifrados/` y `descifrados/`. Mantiene la sesión (usuario y claves RSA activas). |
| `UI` | Ventana Swing: diálogo de contraseña, botones **Cifrar** / **Descifrar**, drag and drop, informe de resultados y apertura automática. No contiene lógica criptográfica; llama a `Main`. |
| `DbManager` | Guarda y busca registros `USUARIO` y `ARCHIVO` en `database.csv`. **No cifra nada**, solo texto. |
| `PasswordManager` | `CodePass(password, salt)`: SHA3-256 de `salt ‖ contraseña`. |
| `RandKeyGenerator` | `GenKey(n)`: `n` bytes aleatorios con `SecureRandom` (claves AES, IV y salt). |
| `cypher.AES` | `Encode` / `Decode` con **AES/GCM/NoPadding**, clave de 128 bits, IV de 12 bytes, etiqueta de 128 bits. |
| `cypher.RSA` | `GenerarParDeClaves` (2048 bits), `Encode` con clave pública, `Decode` con clave privada (**RSA/ECB/PKCS1Padding**). |

El detalle de cada método está en el PDF **`Documentacion_Codigo_Fase1.pdf`**.

## Formato de `database.csv`

Texto con columnas separadas por `;`. Siempre **6 columnas** por línea (el tipo + 5 campos). Todos los datos binarios se guardan en **Base64**.

```
USUARIO;<nombre>;<clavePublica>;<clavePrivadaCifrada>;<salt>;<iv>
ARCHIVO;<nombreOriginal>;<archivoCifrado>;<iv>;<claveAESCifradaConRSA>;<usuarioPropietario>
```

| Campo | Contenido |
|-------|-----------|
| `clavePublica` | Clave pública RSA (formato X.509), **en claro** |
| `clavePrivadaCifrada` | Clave privada RSA (PKCS#8) cifrada con AES-128 |
| `salt` | 32 bytes aleatorios usados con la contraseña |
| `iv` (usuario) | IV de 12 bytes con el que se cifró la clave privada |
| `archivoCifrado` | Contenido del archivo cifrado con AES-128-GCM |
| `iv` (archivo) | IV de 12 bytes de ese archivo |
| `claveAESCifradaConRSA` | Clave AES del archivo cifrada con la clave pública RSA |

## Diseño criptográfico

Notación: `E`/`D` = cifrar/descifrar; `kf` = clave AES del archivo `f`; `Ku`/`ku` = clave pública/privada RSA del usuario `u`; `pu` = contraseña.

**Registro (primer uso)**

1. Se genera el par RSA `(Ku, ku)` de 2048 bits.
2. `salt` = 32 bytes aleatorios; `h = SHA3-256(salt ‖ pu)`.
3. `kp` = 16 primeros bytes de `h` (clave AES-128).
4. Se guarda `Ku` en claro y `E_kp^AES(ku)` junto con `salt` e `iv`.

**Login**: se recalcula `kp` con el `salt` guardado y se descifra `ku`. Con GCM, una contraseña incorrecta provoca un error de autenticación → acceso denegado.

**Cifrar un archivo `f`**

1. `kf` = 16 bytes aleatorios; `iv` = 12 bytes aleatorios.
2. `c = E_kf^AES(f)`.
3. `E_Ku^RSA(kf)`: la clave del archivo se cifra con la clave pública.
4. Se guardan `c`, `iv` y `E_Ku^RSA(kf)` en la base de datos, y `c` en `cifrados/`.

**Descifrar un archivo `f`**

1. `kf = D_ku^RSA( E_Ku^RSA(kf) )`
2. `f = D_kf^AES( c )`

Propiedades: cada archivo tiene clave distinta; la clave AES nunca se almacena en claro; la clave privada nunca se almacena en claro; AES-GCM además **detecta modificaciones** del archivo cifrado.

## Limitaciones conocidas y trabajo futuro

- **Un solo usuario** (Fase I). La Fase II añadirá varios usuarios y compartir archivos cifrando `kf` con la clave pública del destinatario.
- La contraseña se transforma con **un único SHA3-256**; para la Fase II se recomienda una función de derivación de clave (**PBKDF2**) y, opcionalmente, 2FA.
- RSA usa **PKCS#1 v1.5**; **OAEP** es más moderno y sería una mejora.
- Los archivos cifrados se guardan en Base64 dentro del CSV (≈ 33 % más grande); para archivos muy grandes conviene guardar solo la ruta del `.enc`.
- Los archivos se cifran **enteros en memoria**, por lo que vídeos de varios GB podrían agotar la memoria de Java.
- Mejoras posibles: firma digital e integridad de archivos, validación de contraseña (longitud, mayúsculas, números).
