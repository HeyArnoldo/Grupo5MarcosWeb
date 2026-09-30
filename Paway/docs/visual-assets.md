# Recursos visuales y vista previa social

La portada sigue la referencia visual del proyecto: ciudad al atardecer, vehículos de reparto, accesos rojos y paneles informativos. Los recursos se sirven desde la aplicación, sin solicitudes externas para cargar fotografías o ilustraciones.

## Imágenes

| Archivo en `static/images` | Procedencia y uso |
|---|---|
| `logo-paway.jpg` | Logo original de `Avance del Proyecto/Avance1/assets` |
| `paway-city-banner.svg` | Ilustración vectorial creada para el banner de Paway |
| `paway-courier.svg` | Ilustración vectorial creada para el cotizador |
| `warehouse-stock.jpg` | Fotografía de stock de Unsplash: almacén y paquetes |
| `delivery-stock.jpg` | Fotografía de stock de Unsplash: paquetes dentro de un vehículo |
| `paway-social.png` | Imagen de 1200 × 630 para compartir la portada |

Fuentes de las fotografías:

- Almacén: https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d
- Paquetes: https://images.unsplash.com/photo-1580674285054-bed31e145f59
- Licencia de Unsplash: https://unsplash.com/license

Las fotografías son ilustrativas; no representan instalaciones propias de Paway. Se descargaron en JPEG de hasta 1000 px y se cargan de forma diferida cuando aparecen debajo del banner.

## SEO de la portada

La página principal incluye título y descripción, URL canónica, Open Graph, Twitter Card y datos estructurados `Organization`/`WebSite`. Las direcciones públicas utilizan **https://paway.groowtech.com**.

La imagen social se encuentra en:

```text
https://paway.groowtech.com/images/paway-social.png
```

También están disponibles `/robots.txt` y `/sitemap.xml`. El sitemap contiene la portada; las rutas de perfiles, administración y consultas de guías no se anuncian para indexación.

Después de desplegar, verifica que la imagen abra públicamente y que el HTML inicial de la portada incluya los metadatos. Las plataformas de mensajería pueden conservar en caché una vista previa anterior; para comprobar una actualización puedes compartir temporalmente `https://paway.groowtech.com/?preview=2`.
