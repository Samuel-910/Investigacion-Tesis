package com.pe.articulos.core.shared.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings("null")
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        // Carpeta donde se guardarán los archivos
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Directorio de carga de archivos verificado: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos subidos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalizar nombre de archivo
        String originalFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Validar caracteres inválidos
            if (originalFileName.contains("..")) {
                throw new RuntimeException("El nombre del archivo contiene una ruta inválida " + originalFileName);
            }

            // Generar nombre único para evitar colisiones
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i >= 0) {
                fileExtension = originalFileName.substring(i);
            }

            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copiar archivo a la ubicación de destino (reemplazando si existe con el mismo
            // nombre)
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + originalFileName + ". ¡Inténtalo de nuevo!",
                    ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Archivo no encontrado " + fileName, ex);
        }
    }

    /**
     * Almacena un logo con validaciones específicas para imágenes
     * 
     * @param file Archivo de imagen
     * @return Nombre del archivo almacenado
     */
    public String almacenarLogo(MultipartFile file) {
        // Validaciones
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 5MB");
        }

        // Delegar al método genérico
        return storeFile(file);
    }
}
