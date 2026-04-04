# Formato de Versionamiento

Este documento define como trabajaremos el versionamiento de `Contrabajo`, el uso de `CHANGELOG.txt`, la actualizacion del `README.md` y el formato de los commits al momento de publicar avances.

---

## Estructura general

La version seguira este patron:

`Lanzamiento.Actualizacion.Hotfix.Revision-Sufijo`

Ejemplo:

`1.1.1.b-Pre-Alpha`

### Significado de cada parte

- `Lanzamiento`
  Corresponde al numero principal del producto. Sube cuando la aplicacion entra en una nueva etapa importante o una nueva generacion funcional.

- `Actualizacion`
  Corresponde a mejoras relevantes dentro de la version principal actual. Se usa para nuevas funciones, redisenos importantes o cierres de etapas grandes del sistema.

- `Hotfix`
  Corresponde a correcciones concretas dentro de la actualizacion actual. Se usa para arreglos tecnicos, ajustes funcionales o cierres pequenos que ya cambian el estado del sistema.

- `Revision`
  Corresponde a cambios minimos y rapidos. Se representa con una letra de `a` a `z`.
  Ejemplo: `1.1.1.a`, `1.1.1.b`, `1.1.1.c`.

---

## Sufijos de estado

Los sufijos se agregan al final de la version para indicar madurez real del build.

- `-Pre-Alpha`
  La version ya se acerca al objetivo esperado para esa etapa, pero aun puede tener huecos visuales, funcionales o tecnicos.

- `-Alpha`
  La version es funcional y estable para trabajo interno, pero aun no se considera validada con pruebas amplias o publico real.

- `-Beta`
  La version ya fue probada con un grupo mayor de usuarios, mayor carga o validacion mas realista del sistema.

- `-Stable`
  La version se considera lista para liberacion publica o despliegue final.

### Regla importante

Despues de una version `Stable`, cualquier nueva funcionalidad relevante vuelve a entrar en ciclo de madurez.

Ejemplo:

- `1.2-Stable`
- nueva funcionalidad en desarrollo
- `1.3-Pre-Alpha`
- luego `1.3-Alpha`
- luego `1.3-Beta`
- luego `1.3-Stable`

### Versiones sin sufijo

Las versiones sin sufijo deben considerarse internas o incompletas. Se pueden usar temporalmente durante desarrollo, pero no deberian ser la referencia principal del proyecto cuando se documente o publique un avance.

---

## Como trabajar el CHANGELOG

El `CHANGELOG.txt` es el documento vivo del trabajo en curso.

### Regla de uso

1. Mientras se trabaja en la version actual, los cambios se agregan manualmente en el bloque superior del `CHANGELOG.txt`.
2. En ese mismo bloque, al final, se dejan los pendientes con formato `->>>`.
3. Cuando la version esta lista para publicarse, ese bloque superior se considera el registro oficial de esa version.
4. La siguiente version siempre se crea arriba de la anterior.
5. El historial siempre queda ordenado desde la version mas nueva arriba hasta la mas antigua abajo.

### Formato obligatorio del CHANGELOG

```text
❚❙❘ VERSION 0.1-Pre-Alpha
⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺
├─• Cambio 1
├─• Cambio 2
└─• Cambio final

->>> Pendiente 1
->>> Pendiente 2
```

### Reglas del bloque

- La version nueva siempre se escribe arriba.
- Los cambios deben describir lo que realmente se hizo.
- Los pendientes deben describir lo que falta de esa linea de trabajo.
- No mezclar cambios futuros con cambios ya implementados.

---

## Como actualizar el README

El `README.md` siempre debe mostrar solo la version mas reciente en la seccion `Ultimos cambios`.

### Flujo correcto

1. Se trabaja la version actual en `CHANGELOG.txt`.
2. Antes de publicar o pushear, se revisa el bloque mas reciente del changelog.
3. Ese contenido se resume o se replica en la seccion `Ultimos cambios` del `README.md`.
4. El `README.md` debe quedar alineado con la version actual del proyecto.

### Regla principal

El `README.md` no reemplaza al changelog historico.
El `README.md` solo refleja la ultima version.

---

## Como escribir el commit

El commit de publicacion debe incluir el mismo bloque de cambios de la version actual, siguiendo el estilo usado en `Safe Rescue`.

### Formato recomendado

```text
git commit -m "0.1-Pre-Alpha
├─• Cambio 1
├─• Cambio 2
├─• Cambio 3
└─• Cierre o resumen final."
```

### Reglas para commits de version

- El titulo del commit debe ser la version.
- El cuerpo del commit debe resumir los cambios mas importantes de esa version.
- El texto del commit debe coincidir con el estado real del proyecto.
- Si el cambio es muy pequeno, se puede usar una version con letra, por ejemplo `0.1.0.a-Pre-Alpha`.

---

## Flujo de trabajo recomendado

1. Se desarrolla una version.
2. Se van anotando cambios y pendientes en el `CHANGELOG.txt`.
3. Se revisa el estado real de la app.
4. Se actualiza el `README.md` con los ultimos cambios.
5. Se crea el commit con el formato de version.
6. Se hace push.
7. La siguiente version se abre arriba en el `CHANGELOG.txt`.

---

## Criterio recomendado para Contrabajo

Para no romper la forma en que ya viene funcionando el equipo, mantendremos la idea base propuesta, pero con estas mejoras:

- Usar siempre los cuatro niveles solo cuando haga falta.
- No forzar la letra si no hay una correccion realmente minima.
- Mantener el sufijo de madurez siempre visible en versiones que se documenten.
- Evitar publicar versiones sin sufijo salvo que sean internas y temporales.
- Tratar `Pre-Alpha`, `Alpha`, `Beta` y `Stable` como estados reales del producto, no solo decorativos.

Esto deja un versionamiento flexible, compatible con lo que ya vienes usando y mas facil de sostener en equipo.
