🚗💦 AutoWashApp | Lavado Express

AutoWashApp (Lavado Express) es una aplicación móvil Android en producción para la gestión de servicios de lavado de vehículos bajo demanda.
La app conecta clientes, proveedores y administradores, permitiendo reservas, gestión de servicios, pagos y métricas de desempeño en tiempo real.

📱 Tecnologías Utilizadas

Kotlin

Jetpack Compose (Material 3)

Firebase Authentication

Cloud Firestore

Arquitectura Single Activity

Navegación por estado (sin Navigation Component)

Dark Theme

Firebase en tiempo real

🧱 Arquitectura General

Single Activity

UI dividida por roles

Navegación controlada por estado (selectedTab, showScreen)

Código organizado por módulos:

ui/home/user

ui/home/provider

ui/home/admin

No se utiliza Navigation Component (decisión intencional para simplicidad y control total)

👥 Roles y Funcionalidades
🔵 USER (Cliente)

Funciones principales:

Ver servicios disponibles

Crear reservas

Ver Mis Reservas

Seleccionar método de pago

Ver estadísticas personales

Métodos de pago:

💵 Efectivo

🏦 Transferencia

Estados de pedido:

REQUESTED

ACCEPTED

PAYMENT_METHOD_SELECTED

PAYMENT_PROOF_SUBMITTED

PAID

Perfil:

Edición de datos personales

Cambio de contraseña

Foto de perfil con cámara

Eliminación de imagen

Scroll vertical completo

🟠 PROVIDER (Proveedor)

Funciones principales:

Crear, activar y desactivar servicios

Aceptar o rechazar reservas

Confirmar pagos

Gestionar cuenta bancaria

Ver métricas de desempeño

Dashboard de estadísticas:

Total de reservas

Reservas pagadas

Pagos pendientes

Tasa de pago (%)

Nivel del proveedor

Servicios activos / inactivos

Gráfico Donut de estado de reservas

Perfil del proveedor:

Datos personales

Foto de perfil con cámara

Eliminación de imagen

Cambio de contraseña

Scroll vertical completo

👑 ADMIN

Funciones principales:

Gestión de usuarios

Gestión de proveedores

Gestión de servicios

Gestión de pagos

Activar / desactivar cuentas

Cambio de roles

Regla de seguridad:

Si active == false → no puede iniciar sesión

Cierre automático de sesión

🧾 Modelo de Datos (Firestore)
📁 users
{
  "firstName": "String",
  "lastName": "String",
  "phone": "String",
  "email": "String",
  "role": "USER | PROVIDER | ADMIN",
  "active": true,
  "photoBase64": "String (opcional)",
  "bankInfo": { }
}

📁 services
{
  "providerId": "String",
  "title": "String",
  "description": "String",
  "durationMinutes": Number,
  "price": Number,
  "active": Boolean
}

📁 orders
{
  "userId": "String",
  "providerId": "String",
  "serviceTitle": "String",
  "serviceDate": "dd/MM/yyyy",
  "serviceTime": "HH:mm",
  "paymentMethod": "CASH | TRANSFER",
  "paymentMethodLocked": Boolean,
  "transferCode": "String?",
  "status": "REQUESTED | ACCEPTED | PAYMENT_METHOD_SELECTED | PAYMENT_PROOF_SUBMITTED | PAID"
}

📷 Foto de Perfil (Avatar)

Captura mediante cámara del dispositivo

Conversión a Base64

Guardado directo en Firestore

❌ No se usa Firebase Storage

❌ No genera costos adicionales

Imagen opcional

Botón para eliminar imagen

Avatar por defecto cuando no existe foto

🔐 Seguridad

Autenticación con Firebase Auth

Reglas basadas en rol

Acceso controlado a colecciones

Eliminación real de campos (FieldValue.delete())

🚀 Ejecución del Proyecto
Ejecutar en emulador o dispositivo real
Run ▶ desde Android Studio

Generar APK
Build → Generate App Bundles or APKs → APK


Ruta:

app/build/outputs/apk/debug/app-debug.apk

🌐 Firebase

Base de datos en tiempo real

Cambios visibles inmediatamente en consola

Mismo proyecto para:

Emulador

APK

Dispositivo físico

🎯 Estado del Proyecto

✅ Funcional
✅ UI pulida
✅ Arquitectura estable
✅ Sin deuda técnica crítica
✅ Listo para testers / despliegue

📌 Decisiones de Diseño

No Navigation Component (control explícito del flujo)

UI separada por rol

Cambios incrementales

UX prioritaria sobre complejidad técnica

Costo Firebase optimizado (0 uso de Storage)

📄 Licencia

Proyecto académico / experimental / demostrativo.
Uso libre para aprendizaje y referencia.
