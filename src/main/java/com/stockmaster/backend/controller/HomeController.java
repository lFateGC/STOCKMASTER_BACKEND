package com.stockmaster.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>StockMaster API - Servidor Backend</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    font-family: 'Inter', sans-serif;
                    background-color: #0f172a;
                    color: #e2e8f0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    min-height: 100vh;
                    padding: 24px;
                }
                .container {
                    max-width: 800px;
                    width: 100%;
                    background: #1e293b;
                    border: 1px solid #334155;
                    border-radius: 16px;
                    padding: 40px;
                    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
                }
                .header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    border-bottom: 1px solid #334155;
                    padding-bottom: 24px;
                    margin-bottom: 28px;
                    flex-wrap: wrap;
                    gap: 16px;
                }
                .brand {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                }
                .logo-icon {
                    width: 48px;
                    height: 48px;
                    background: linear-gradient(135deg, #3b82f6, #06b6d4);
                    border-radius: 12px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 24px;
                }
                .brand-text h1 {
                    font-size: 24px;
                    font-weight: 700;
                    color: #ffffff;
                }
                .brand-text p {
                    font-size: 14px;
                    color: #94a3b8;
                }
                .status-badge {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    background: rgba(16, 185, 129, 0.15);
                    color: #34d399;
                    border: 1px solid rgba(16, 185, 129, 0.3);
                    padding: 6px 14px;
                    border-radius: 9999px;
                    font-size: 13px;
                    font-weight: 600;
                }
                .status-dot {
                    width: 8px;
                    height: 8px;
                    background: #10b981;
                    border-radius: 50%;
                    box-shadow: 0 0 10px #10b981;
                }
                .grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                    gap: 16px;
                    margin-bottom: 32px;
                }
                .card {
                    background: #0f172a;
                    border: 1px solid #334155;
                    border-radius: 12px;
                    padding: 16px;
                }
                .card-title {
                    font-size: 13px;
                    color: #94a3b8;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 6px;
                }
                .card-value {
                    font-size: 16px;
                    font-weight: 600;
                    color: #f1f5f9;
                }
                .endpoints-section {
                    background: #0f172a;
                    border: 1px solid #334155;
                    border-radius: 12px;
                    padding: 20px;
                    margin-bottom: 28px;
                }
                .endpoints-section h2 {
                    font-size: 16px;
                    font-weight: 600;
                    color: #f8fafc;
                    margin-bottom: 14px;
                }
                .endpoint-row {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    padding: 8px 0;
                    border-bottom: 1px solid #1e293b;
                    font-size: 13px;
                }
                .endpoint-row:last-child { border-bottom: none; }
                .method {
                    font-weight: 700;
                    font-size: 11px;
                    padding: 3px 8px;
                    border-radius: 4px;
                    min-width: 50px;
                    text-align: center;
                }
                .post { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
                .get  { background: rgba(16, 185, 129, 0.2); color: #34d399; }
                .path { font-family: monospace; color: #cbd5e1; }
                .desc { color: #94a3b8; margin-left: auto; }
                .footer {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    font-size: 13px;
                    color: #64748b;
                    flex-wrap: wrap;
                    gap: 12px;
                }
                .btn-repo {
                    background: #3b82f6;
                    color: #ffffff;
                    text-decoration: none;
                    padding: 8px 16px;
                    border-radius: 8px;
                    font-size: 13px;
                    font-weight: 500;
                    transition: background 0.2s;
                }
                .btn-repo:hover {
                    background: #2563eb;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div class="brand">
                        <div class="logo-icon">📦</div>
                        <div class="brand-text">
                            <h1>StockMaster API</h1>
                            <p>Servidor Backend de Gestión de Inventarios y Ventas</p>
                        </div>
                    </div>
                    <div class="status-badge">
                        <span class="status-dot"></span>
                        Servicio en línea
                    </div>
                </div>

                <div class="grid">
                    <div class="card">
                        <div class="card-title">Base de Datos</div>
                        <div class="card-value">PostgreSQL (Supabase)</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Arquitectura</div>
                        <div class="card-value">Spring Boot 4 / Java 21</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Seguridad</div>
                        <div class="card-value">JWT + BCrypt + AES-256</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Servidor Cloud</div>
                        <div class="card-value">Render Web Service</div>
                    </div>
                </div>

                <div class="endpoints-section">
                    <h2>Rutas Principales del API REST</h2>
                    <div class="endpoint-row">
                        <span class="method post">POST</span>
                        <span class="path">/api/auth/login</span>
                        <span class="desc">Autenticación de usuario con JWT</span>
                    </div>
                    <div class="endpoint-row">
                        <span class="method post">POST</span>
                        <span class="path">/api/auth/register</span>
                        <span class="desc">Registro de nuevo usuario</span>
                    </div>
                    <div class="endpoint-row">
                        <span class="method get">GET</span>
                        <span class="path">/api/productos</span>
                        <span class="desc">Catálogo de productos con imágenes</span>
                    </div>
                    <div class="endpoint-row">
                        <span class="method get">GET</span>
                        <span class="path">/api/dashboard/stats</span>
                        <span class="desc">Métricas y KPIs del inventario</span>
                    </div>
                    <div class="endpoint-row">
                        <span class="method post">POST</span>
                        <span class="path">/api/ventas</span>
                        <span class="desc">Registro de venta y descuento de stock</span>
                    </div>
                </div>

                <div class="footer">
                    <span>StockMaster &copy; 2026 &bull; Todos los derechos reservados</span>
                    <a href="https://github.com/lFateGC/StockMaster_Backend" target="_blank" class="btn-repo">Ver Repositorio GitHub &rarr;</a>
                </div>
            </div>
        </body>
        </html>
        """;
    }
}
