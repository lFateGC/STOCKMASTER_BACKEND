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
            <title>StockMaster — API REST</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    font-family: 'Inter', system-ui, -apple-system, sans-serif;
                    background-color: #0f172a;
                    color: #e2e8f0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    min-height: 100vh;
                    padding: 32px 16px;
                }
                .container {
                    max-width: 860px;
                    width: 100%;
                    background: #1e293b;
                    border: 1px solid #334155;
                    border-radius: 12px;
                    padding: 36px 40px;
                    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
                }
                .header {
                    display: flex;
                    align-items: flex-start;
                    justify-content: space-between;
                    border-bottom: 1px solid #334155;
                    padding-bottom: 20px;
                    margin-bottom: 24px;
                    flex-wrap: wrap;
                    gap: 16px;
                }
                .title-area h1 {
                    font-size: 22px;
                    font-weight: 700;
                    color: #ffffff;
                    letter-spacing: -0.3px;
                }
                .title-area p {
                    font-size: 14px;
                    color: #94a3b8;
                    margin-top: 4px;
                }
                .badge-status {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    background: rgba(16, 185, 129, 0.12);
                    color: #34d399;
                    border: 1px solid rgba(16, 185, 129, 0.25);
                    padding: 6px 14px;
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: 600;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                .dot {
                    width: 7px;
                    height: 7px;
                    background: #10b981;
                    border-radius: 50%;
                }
                .section-title {
                    font-size: 13px;
                    font-weight: 600;
                    color: #94a3b8;
                    text-transform: uppercase;
                    letter-spacing: 0.6px;
                    margin-bottom: 12px;
                }
                .spec-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                    gap: 12px;
                    margin-bottom: 28px;
                }
                .spec-card {
                    background: #0f172a;
                    border: 1px solid #334155;
                    border-radius: 8px;
                    padding: 14px 16px;
                }
                .spec-label {
                    font-size: 11px;
                    color: #64748b;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 4px;
                }
                .spec-value {
                    font-size: 14px;
                    font-weight: 600;
                    color: #f1f5f9;
                }
                .table-container {
                    background: #0f172a;
                    border: 1px solid #334155;
                    border-radius: 8px;
                    overflow: hidden;
                    margin-bottom: 28px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 13px;
                    text-align: left;
                }
                th {
                    background: #1e293b;
                    color: #94a3b8;
                    font-weight: 600;
                    padding: 10px 16px;
                    text-transform: uppercase;
                    font-size: 11px;
                    letter-spacing: 0.5px;
                    border-bottom: 1px solid #334155;
                }
                td {
                    padding: 11px 16px;
                    border-bottom: 1px solid #1e293b;
                }
                tr:last-child td { border-bottom: none; }
                .method-badge {
                    font-size: 11px;
                    font-weight: 700;
                    padding: 3px 8px;
                    border-radius: 4px;
                    display: inline-block;
                    font-family: monospace;
                }
                .method-post { background: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); }
                .method-get  { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }
                .endpoint-path {
                    font-family: 'Consolas', 'Courier New', monospace;
                    color: #f8fafc;
                    font-size: 12px;
                }
                .access-tag {
                    font-size: 11px;
                    color: #94a3b8;
                }
                .footer {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    font-size: 12px;
                    color: #64748b;
                    flex-wrap: wrap;
                    gap: 12px;
                    padding-top: 12px;
                }
                .repo-link {
                    color: #38bdf8;
                    text-decoration: none;
                    font-weight: 500;
                    border: 1px solid #334155;
                    padding: 6px 12px;
                    border-radius: 6px;
                    transition: border-color 0.2s, background 0.2s;
                }
                .repo-link:hover {
                    border-color: #38bdf8;
                    background: rgba(56, 189, 248, 0.05);
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <div class="title-area">
                        <h1>StockMaster &mdash; Plataforma de Servicios REST</h1>
                        <p>Módulo de Backend, Seguridad y Lógica de Negocio</p>
                    </div>
                    <div class="badge-status">
                        <span class="dot"></span>
                        Estado: Activo
                    </div>
                </div>

                <div class="section-title">Especificaciones Técnicas</div>
                <div class="spec-grid">
                    <div class="spec-card">
                        <div class="spec-label">Entorno</div>
                        <div class="spec-value">Java 21 / Spring Boot</div>
                    </div>
                    <div class="spec-card">
                        <div class="spec-label">Base de Datos</div>
                        <div class="spec-value">PostgreSQL (Supabase)</div>
                    </div>
                    <div class="spec-card">
                        <div class="spec-label">Autenticación</div>
                        <div class="spec-value">JWT / BCrypt</div>
                    </div>
                    <div class="spec-card">
                        <div class="spec-label">Cifrado de Datos</div>
                        <div class="spec-value">AES-256-GCM</div>
                    </div>
                </div>

                <div class="section-title">Catálogo de Servicios API</div>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 90px;">Método</th>
                                <th>Ruta</th>
                                <th>Descripción</th>
                                <th style="width: 140px;">Acceso</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><span class="method-badge method-post">POST</span></td>
                                <td class="endpoint-path">/api/auth/login</td>
                                <td>Autenticación de usuario y retorno de JWT</td>
                                <td class="access-tag">Público</td>
                            </tr>
                            <tr>
                                <td><span class="method-badge method-post">POST</span></td>
                                <td class="endpoint-path">/api/auth/register</td>
                                <td>Registro de nuevos usuarios</td>
                                <td class="access-tag">Público</td>
                            </tr>
                            <tr>
                                <td><span class="method-badge method-get">GET</span></td>
                                <td class="endpoint-path">/api/productos</td>
                                <td>Consulta de catálogo de inventario</td>
                                <td class="access-tag">Bearer Token</td>
                            </tr>
                            <tr>
                                <td><span class="method-badge method-post">POST</span></td>
                                <td class="endpoint-path">/api/ventas</td>
                                <td>Procesamiento transaccional de ventas</td>
                                <td class="access-tag">Bearer Token</td>
                            </tr>
                            <tr>
                                <td><span class="method-badge method-get">GET</span></td>
                                <td class="endpoint-path">/api/dashboard/stats</td>
                                <td>Consolidado de métricas comerciales</td>
                                <td class="access-tag">Bearer Token</td>
                            </tr>
                            <tr>
                                <td><span class="method-badge method-get">GET</span></td>
                                <td class="endpoint-path">/api/movimientos</td>
                                <td>Auditoría de movimientos de stock (Kardex)</td>
                                <td class="access-tag">Bearer Token (Admin)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="footer">
                    <span>StockMaster &bull; Versión 1.0.0-RELEASE</span>
                    <a href="https://github.com/lFateGC/StockMaster_Backend" target="_blank" class="repo-link">Repositorio del Proyecto</a>
                </div>
            </div>
        </body>
        </html>
        """;
    }
}
