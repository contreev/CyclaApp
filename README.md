# CyclApp

**CyclApp** es una aplicación Android moderna diseñada para fomentar el reciclaje y la gestión responsable de residuos. La aplicación permite a los usuarios localizar puntos de reciclaje, identificar tipos de residuos mediante inteligencia artificial y participar en misiones para mejorar su impacto ambiental.

## Caracteristicas Principales

- **Mapa de Puntos de Reciclaje:** Localiza puntos de recolección en la ciudad (integrado con datos de Bogotá) utilizando OpenStreetMap.
- **Identificación de Residuos (IA):** Cámara inteligente integrada con TensorFlow Lite para clasificar residuos en tiempo real.
- **Misiones y Logros:** Gamificación para motivar a los usuarios a completar tareas de reciclaje.
- **Noticias y Comunidad:** Mantente informado con las últimas noticias sobre sostenibilidad.
- **Perfil de Usuario:** Seguimiento personalizado de actividades y progreso.
- **Autenticación Completa:** Registro, inicio de sesión y recuperación de contraseña con Firebase.

## Tecnologias Utilizadas

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Arquitectura moderna y declarativa)
- **Backend:** [Firebase](https://firebase.google.com/) (Auth, Firestore, Storage)
- **Mapas:** [osmdroid](https://github.com/osmdroid/osmdroid) & Google Maps API
- **IA/ML:** [TensorFlow Lite](https://www.tensorflow.org/lite)
- **Redes:** [Retrofit](https://square.github.io/retrofit/) para consumo de APIs REST
- **Cámara:** [CameraX](https://developer.android.com/training/camerax)
- **Carga de Imágenes:** [Coil](https://coil-kt.github.io/coil/)

## Requisitos Previos

- **Android Studio** Ladybug o superior.
- **JDK 11** o superior.
- **Dispositivo Android** con API 24 (Android 7.0) o superior.
- **Cuenta de Firebase** (para servicios de backend).

## Configuracion e Instalacion

### 1. Clonar el repositorio
bash git clone https://github.com/tu-usuario/CyclaApp.git


### 3. Llaves de API
En el archivo `app/src/main/AndroidManifest.xml`, asegúrate de configurar tu llave de Google Maps si deseas usar ese proveedor:
xml <meta-data
android:name="com.google.android.geo.API_KEY"
android:value="TU_API_KEY_AQUÍ" />

### 4. Sincronizacion de Datos
Al ejecutar la app por primera vez, `MainActivity` llamará a `sincronizarTodosLosPuntosDeBogota()`. Esto cargará los puntos de reciclaje oficiales desde la API de Datos Abiertos de Bogotá a tu instancia de Firebase Firestore.

## Como ejecutar la App

1. Abre el proyecto en **Android Studio**.
2. Deja que Gradle sincronice las dependencias.
3. Conecta un dispositivo físico o inicia un emulador.
4. Presiona el botón **Run**.

## Estructura del Proyecto

- `components/`: Componentes de UI reutilizables (botones, tarjetas, barras).
- `data/`: Lógica de sincronización de datos y repositorios.
- `model/`: Clases de datos (UserProfile, MissionItem, etc.).
- `navigation/`: Configuración de NavHost y rutas de la app.
- `screens/`: Pantallas principales (Map, Auth, Camera, News).
- `viewmodel/`: Lógica de negocio y manejo de estado de la UI.
