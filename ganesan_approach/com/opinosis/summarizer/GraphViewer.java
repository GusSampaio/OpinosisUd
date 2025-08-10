package com.opinosis.summarizer;

import com.opinosis.Node;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GraphViewer extends JFrame {
    private SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;
    private Map<Node, Point2D> vertexPositions;
    private static final int VERTEX_RADIUS = 15;
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    // Variáveis para zoom e interatividade
    private double zoom = 1.0;
    private Node selectedNode = null;
    private Point2D.Double offset = new Point2D.Double(0, 0);
    private Point lastMousePosition = null;
    private boolean isDragging = false;
    private Set<DefaultWeightedEdge> connectedEdges = new HashSet<>();
    private double baseVertexRadius = VERTEX_RADIUS;

    public GraphViewer(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph) {
        this.graph = graph;
        this.vertexPositions = new HashMap<>();

        setTitle("Visualização do Grafo");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Cria a barra de ferramentas
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);

        // Calcula posições dos vértices em um layout circular
        calculateVertexPositions();

        // Adiciona o painel de desenho
        GraphPanel panel = new GraphPanel();
        add(panel, BorderLayout.CENTER);
        
        // Adiciona listeners para interatividade
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point2D mousePos = screenToGraph(e.getPoint());
                selectedNode = findNodeAt(mousePos);
                updateConnectedEdges();
                panel.repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePosition = e.getPoint();
                isDragging = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });
        
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && lastMousePosition != null) {
                    double dx = (e.getX() - lastMousePosition.x) / zoom;
                    double dy = (e.getY() - lastMousePosition.y) / zoom;
                    offset.x += dx;
                    offset.y += dy;
                    lastMousePosition = e.getPoint();
                    panel.repaint();
                }
            }
        });
        
        panel.addMouseWheelListener(e -> {
            double oldZoom = zoom;
            double zoomFactor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            zoom *= zoomFactor;
            zoom = Math.max(0.1, Math.min(5.0, zoom));
            
            // Ajusta o tamanho dos vértices baseado no zoom
            baseVertexRadius = VERTEX_RADIUS * (1.0 + (zoom - 1.0) * 0.5);
            
            // Ajusta o offset para manter o ponto sob o cursor
            Point mousePos = e.getPoint();
            double dx = (mousePos.x - WINDOW_WIDTH/2) * (1 - zoom/oldZoom);
            double dy = (mousePos.y - WINDOW_HEIGHT/2) * (1 - zoom/oldZoom);
            offset.x += dx;
            offset.y += dy;
            
            panel.repaint();
        });
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // Botão de zoom in
        JButton zoomInButton = new JButton("+");
        zoomInButton.addActionListener(e -> {
            zoom *= 1.2;
            zoom = Math.min(5.0, zoom);
            baseVertexRadius = VERTEX_RADIUS * (1.0 + (zoom - 1.0) * 0.5);
            repaint();
        });
        
        // Botão de zoom out
        JButton zoomOutButton = new JButton("-");
        zoomOutButton.addActionListener(e -> {
            zoom /= 1.2;
            zoom = Math.max(0.1, zoom);
            baseVertexRadius = VERTEX_RADIUS * (1.0 + (zoom - 1.0) * 0.5);
            repaint();
        });
        
        // Botão de reset
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            zoom = 1.0;
            offset.x = 0;
            offset.y = 0;
            baseVertexRadius = VERTEX_RADIUS;
            selectedNode = null;
            connectedEdges.clear();
            repaint();
        });
        
        toolBar.add(zoomInButton);
        toolBar.add(zoomOutButton);
        toolBar.addSeparator();
        toolBar.add(resetButton);
        
        return toolBar;
    }

    private void calculateVertexPositions() {
        int vertexCount = graph.vertexSet().size();
        double angleStep = 2 * Math.PI / vertexCount;
        double radius = Math.min(WINDOW_WIDTH, WINDOW_HEIGHT) / 2.5 * (1 + Math.log(vertexCount) / 10);

        int i = 0;
        for (Node vertex : graph.vertexSet()) {
            double angle = i * angleStep;
            double x = WINDOW_WIDTH/2 + radius * Math.cos(angle);
            double y = WINDOW_HEIGHT/2 + radius * Math.sin(angle);
            vertexPositions.put(vertex, new Point2D.Double(x, y));
            i++;
        }
    }

    private void updateConnectedEdges() {
        connectedEdges.clear();
        if (selectedNode != null) {
            connectedEdges.addAll(graph.outgoingEdgesOf(selectedNode));
            connectedEdges.addAll(graph.incomingEdgesOf(selectedNode));
        }
    }

    private Point2D screenToGraph(Point screenPoint) {
        return new Point2D.Double(
            (screenPoint.x - WINDOW_WIDTH/2) / zoom - offset.x + WINDOW_WIDTH/2,
            (screenPoint.y - WINDOW_HEIGHT/2) / zoom - offset.y + WINDOW_HEIGHT/2
        );
    }

    private Point graphToScreen(Point2D graphPoint) {
        return new Point(
            (int)((graphPoint.getX() + offset.x - WINDOW_WIDTH/2) * zoom + WINDOW_WIDTH/2),
            (int)((graphPoint.getY() + offset.y - WINDOW_HEIGHT/2) * zoom + WINDOW_HEIGHT/2)
        );
    }

    private Node findNodeAt(Point2D point) {
        for (Node vertex : graph.vertexSet()) {
            Point2D pos = vertexPositions.get(vertex);
            double dx = point.getX() - pos.getX();
            double dy = point.getY() - pos.getY();
            if (dx * dx + dy * dy <= baseVertexRadius * baseVertexRadius) {
                return vertex;
            }
        }
        return null;
    }

    private class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Aplica transformação de zoom e offset
            g2d.translate(WINDOW_WIDTH/2, WINDOW_HEIGHT/2);
            g2d.scale(zoom, zoom);
            g2d.translate(-WINDOW_WIDTH/2, -WINDOW_HEIGHT/2);
            g2d.translate(offset.x, offset.y);

            // Desenha as arestas
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                Node source = graph.getEdgeSource(edge);
                Node target = graph.getEdgeTarget(edge);
                Point2D sourcePos = vertexPositions.get(source);
                Point2D targetPos = vertexPositions.get(target);

                // Destaca as arestas conectadas ao nó selecionado
                if (connectedEdges.contains(edge)) {
                    g2d.setColor(new Color(255, 100, 100));
                    g2d.setStroke(new BasicStroke(2.0f / (float)zoom));
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1.0f / (float)zoom));
                }

                // Desenha a linha
                g2d.drawLine(
                        (int) sourcePos.getX(),
                        (int) sourcePos.getY(),
                        (int) targetPos.getX(),
                        (int) targetPos.getY()
                );

                // Desenha a seta
                double angle = Math.atan2(targetPos.getY() - sourcePos.getY(), targetPos.getX() - sourcePos.getX());
                double arrowLength = 8.0 / zoom;
                double arrowWidth = 4.0 / zoom;
                
                // Calcula o ponto onde a seta deve começar (um pouco antes do nó de destino)
                double distance = Math.sqrt(
                    Math.pow(targetPos.getX() - sourcePos.getX(), 2) + 
                    Math.pow(targetPos.getY() - sourcePos.getY(), 2)
                );
                double arrowStartX = targetPos.getX() - (baseVertexRadius * Math.cos(angle));
                double arrowStartY = targetPos.getY() - (baseVertexRadius * Math.sin(angle));
                
                // Calcula os pontos da seta
                double[] xPoints = new double[3];
                double[] yPoints = new double[3];
                
                xPoints[0] = arrowStartX;
                yPoints[0] = arrowStartY;
                
                xPoints[1] = arrowStartX - arrowLength * Math.cos(angle - Math.PI/6);
                yPoints[1] = arrowStartY - arrowLength * Math.sin(angle - Math.PI/6);
                
                xPoints[2] = arrowStartX - arrowLength * Math.cos(angle + Math.PI/6);
                yPoints[2] = arrowStartY - arrowLength * Math.sin(angle + Math.PI/6);
                
                // Desenha a seta
                g2d.fill(new java.awt.geom.Path2D.Double(
                    java.awt.geom.Path2D.WIND_EVEN_ODD,
                    xPoints.length
                ) {{
                    moveTo(xPoints[0], yPoints[0]);
                    lineTo(xPoints[1], yPoints[1]);
                    lineTo(xPoints[2], yPoints[2]);
                    closePath();
                }});

                // Desenha o peso da aresta
                double weight = graph.getEdgeWeight(edge);
                Point2D midPoint = new Point2D.Double(
                        (sourcePos.getX() + targetPos.getX()) / 2,
                        (sourcePos.getY() + targetPos.getY()) / 2
                );
                Font originalFont = g2d.getFont();
                g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() / (float)zoom));
                g2d.drawString(String.format("%.2f", weight),
                        (int) midPoint.getX(),
                        (int) midPoint.getY());
                g2d.setFont(originalFont);
            }

            // Desenha os vértices
            for (Node vertex : graph.vertexSet()) {
                Point2D pos = vertexPositions.get(vertex);

                // Desenha o círculo
                if (vertex == selectedNode) {
                    g2d.setColor(new Color(255, 200, 200)); // Rosa claro para nó selecionado
                } else if (connectedEdges.stream().anyMatch(edge -> 
                    graph.getEdgeSource(edge) == vertex)) {
                    g2d.setColor(new Color(200, 255, 200)); // Verde claro para nós de saída
                } else if (connectedEdges.stream().anyMatch(edge -> 
                    graph.getEdgeTarget(edge) == vertex)) {
                    g2d.setColor(new Color(200, 200, 255)); // Azul claro para nós de entrada
                } else {
                    g2d.setColor(Color.LIGHT_GRAY);
                }
                
                g2d.fillOval(
                        (int) pos.getX() - (int)baseVertexRadius,
                        (int) pos.getY() - (int)baseVertexRadius,
                        (int)(baseVertexRadius * 2),
                        (int)(baseVertexRadius * 2)
                );

                // Desenha a borda
                g2d.setColor(Color.BLACK);
                g2d.drawOval(
                        (int) pos.getX() - (int)baseVertexRadius,
                        (int) pos.getY() - (int)baseVertexRadius,
                        (int)(baseVertexRadius * 2),
                        (int)(baseVertexRadius * 2)
                );

                // Desenha o texto
                String label = vertex.getNodeName();
                Font originalFont = g2d.getFont();
                g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() / (float)zoom));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                g2d.drawString(label,
                        (int) pos.getX() - textWidth/2,
                        (int) pos.getY() + fm.getAscent()/2);
                g2d.setFont(originalFont);
            }

            // Desenha informações do nó selecionado
            if (selectedNode != null) {
                g2d.setColor(Color.BLACK);
                int inDegree = graph.inDegreeOf(selectedNode);
                int outDegree = graph.outDegreeOf(selectedNode);
                String info = String.format("Grau de entrada: %d, Grau de saída: %d", inDegree, outDegree);
                Font originalFont = g2d.getFont();
                g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() / (float)zoom));
                g2d.drawString(info, 10, 20);
                g2d.setFont(originalFont);
            }

            // Desenha a legenda
            drawLegend(g2d);
            
            // Desenha o nível de zoom
            String zoomText = String.format("Zoom: %.1fx", zoom);
            g2d.setColor(Color.BLACK);
            Font originalFont = g2d.getFont();
            g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() / (float)zoom));
            g2d.drawString(zoomText, WINDOW_WIDTH - 100, 20);
            g2d.setFont(originalFont);
        }

        private void drawLegend(Graphics2D g2d) {
            int legendX = 10;
            int legendY = WINDOW_HEIGHT - 120;
            int boxSize = 15;
            int spacing = 25;
            
            Font originalFont = g2d.getFont();
            g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() / (float)zoom));
            
            // Nó selecionado
            g2d.setColor(new Color(255, 200, 200));
            g2d.fillOval(legendX, legendY, boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendX, legendY, boxSize, boxSize);
            g2d.drawString("Nó selecionado", legendX + boxSize + 5, legendY + boxSize/2 + 5);
            
            // Nó de saída
            g2d.setColor(new Color(200, 255, 200));
            g2d.fillOval(legendX, legendY + spacing, boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendX, legendY + spacing, boxSize, boxSize);
            g2d.drawString("Nó de saída", legendX + boxSize + 5, legendY + spacing + boxSize/2 + 5);
            
            // Nó de entrada
            g2d.setColor(new Color(200, 200, 255));
            g2d.fillOval(legendX, legendY + spacing * 2, boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendX, legendY + spacing * 2, boxSize, boxSize);
            g2d.drawString("Nó de entrada", legendX + boxSize + 5, legendY + spacing * 2 + boxSize/2 + 5);
            
            g2d.setFont(originalFont);
        }
    }
}