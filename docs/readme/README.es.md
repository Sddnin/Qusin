<!-- translated-from: README.md @ 1e3fabff8c4e -->
<div align="center">

<img src="../media/logo.png" alt="Qusin" width="88" />

# Qusin

**Qusin es un asistente de IA para Android.** Describe una tarea con lenguaje natural y te ayudará a usar aplicaciones, sitios web, archivos y tareas programadas.

Qusin no incluye un modelo de IA propio. Puedes conectar un proveedor con tu clave API, iniciar sesión con una cuenta compatible o usar un modelo alojado en tu ordenador o red local.

## Funciones

- **Control de aplicaciones Android:** opera las aplicaciones en una pantalla virtual en segundo plano, lee la interfaz, toca, escribe y desplaza contenido mientras sigues usando la pantalla principal. Hay límites del dispositivo y de cada aplicación; el inicio de sesión, los CAPTCHA y los pasos sensibles pueden requerir tu intervención.
- **Navegador integrado:** usa un WebView independiente que conserva las sesiones de los sitios y admite eventos de entrada reales, lectura de páginas, cargas y descargas.
- **Tareas largas y paralelas:** consulta el progreso y los pasos ejecutados, envía instrucciones adicionales o detén una tarea. El paralelismo está limitado por el dispositivo y las aplicaciones.
- **Horarios y condiciones:** crea tareas únicas, diarias, semanales o basadas en condiciones; consulta la próxima ejecución y el historial.
- **Memoria y habilidades:** guarda información autorizada, consulta conocimientos sobre aplicaciones y sitios, y añade habilidades de texto.
- **Espacio de trabajo Linux:** cada conversación tiene un espacio propio para archivos y comandos. El entorno integrado solo admite actualmente dispositivos ARM64.
- **Conexión con el ordenador:** envía tareas mediante CLI/Skill o MCP, sigue su estado y recibe los resultados desde el ordenador.

## Modelos y claves API

Qusin admite los protocolos compatibles con OpenAI Chat Completions, Anthropic Messages, Google Gemini `generateContent` y OpenAI Responses. Incluye plantillas para varios proveedores en la nube, servicios compatibles con OpenAI y servidores locales como Ollama/LM Studio; también admite endpoints personalizados.

Abre **Ajustes → Modelos → elige un proveedor** y usa **Grupo de claves API → Añadir clave API** para añadir claves. Puedes pegar varias separadas por comas, punto y coma o saltos de línea. Qusin alterna entre las claves disponibles: `401/403` marca una clave como rechazada y `429` la pone en espera según la respuesta del servidor. Si ninguna está disponible, no vuelve a usar una clave rechazada.

El grupo se aplica a los protocolos compatibles con claves API. Las sesiones OAuth, como los inicios de sesión de ChatGPT y xAI, no son claves API y se administran por separado. OpenRouter permite conservar una cuenta con sesión iniciada y añadir claves de respaldo independientes.

## Conocimientos integrados

La lista integrada de aplicaciones, sitios web y habilidades generales se genera a partir de las fuentes de conocimiento del proyecto y se actualiza con esos datos.

## Proveedores preconfigurados

Hay plantillas para DeepSeek, Qwen, Kimi, Zhipu, SiliconFlow, Volcano Engine, MiniMax, StepFun, Tencent Hunyuan, Baidu Qianfan, OpenRouter, OpenDesign Go, OpenCode Go, Anthropic, Gemini, OpenAI, xAI, Mistral, Groq, Cloudflare Workers AI, Ollama y LM Studio. También puedes configurar un endpoint personalizado.

## Requisitos

- Android 10 o posterior para la instalación y las funciones básicas.
- Android 12 o posterior y completar el asistente de configuración para controlar otras aplicaciones Android.
- Un dispositivo ARM64 para el entorno Linux integrado.
- Un modelo principal que admita llamadas a herramientas/funciones.

## Primeros pasos

1. Instala Qusin y ábrelo en tu teléfono.
2. Ve a **Ajustes → Modelos** y añade un proveedor o inicia sesión con una cuenta compatible.
3. Selecciona un modelo principal compatible con llamadas a herramientas. Los modelos de visión, ligeros y de voz a texto se configuran por separado.
4. Para controlar aplicaciones Android, completa el emparejamiento de depuración inalámbrica y la configuración de accesibilidad dentro de la aplicación.
5. Inicia una conversación y describe la tarea en lenguaje natural.

Documentación:

- [Modelos e inicio de sesión](../models.en.md)
- [Configuración del control del teléfono](../setup-guide.en.md)
- [Referencia de funciones](../features.en.md)
- [Preguntas frecuentes](../faq.en.md)
- [Usar Qusin desde un ordenador](../computer.en.md)
- [Hoja de ruta de desarrollo](../dev/roadmap.md)

## Notas de versión

Las descripciones de GitHub Releases pueden incluir secciones etiquetadas por idioma. Qusin muestra la sección correspondiente al idioma de la aplicación y recurre al inglés si no existe. Para publicar una versión, utiliza la [plantilla de notas multilingües](../dev/release-notes-template.md).

## Compilar desde el código fuente

Necesitas JDK 17 y un Android SDK que coincida con la configuración del proyecto Gradle.

```bash
# Pruebas unitarias
./gradlew :core:test :app:testDebugUnitTest

# Crear un APK Debug
./gradlew :app:assembleDebug
```

El APK Debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Privacidad y limitaciones

Las claves API y los secretos se cifran al guardarse en el dispositivo. El proveedor que configures procesa las instrucciones y otros datos enviados al modelo; Qusin no puede sustituir la política de tratamiento de datos de ese proveedor. Las tareas de control del teléfono usan los permisos Android que concedas. Es posible que no se puedan leer pantallas seguras, contenido DRM o interfaces que bloqueen las capturas, y que tengas que intervenir.

Qusin sigue en desarrollo. Comprueba los resultados antes de confiar en operaciones importantes, especialmente enviar mensajes, borrar datos, hacer pedidos o pagar.

## Licencia

Consulta [LICENSE](../../LICENSE), [NOTICE](../../NOTICE) y los [avisos de licencias de terceros](../../licenses/THIRD_PARTY_NOTICES.md).
