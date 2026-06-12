package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.repository.CategoryRepository;

@SpringBootApplication
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Bean
	public CommandLineRunner fixAndSeedDatabase(JdbcTemplate jdbcTemplate, CategoryRepository categoryRepository) {
		return args -> {
			try {
				System.out.println("Applying Database Schema Fix: product.description & offer.description -> TEXT");
				// H2-compatible schema fix
				try {
					jdbcTemplate.execute("ALTER TABLE product ALTER COLUMN description VARCHAR(16384)");
					jdbcTemplate.execute("ALTER TABLE offer ALTER COLUMN description VARCHAR(16384)");
					System.out.println("Schema fix applied successfully!");
				} catch (Exception e) {
					System.out.println("Schema fix skipped (ok): " + e.getMessage());
				}

				// Seed Categories
				String[] defaultCategories = {
					"Cybernetic Enhancements",
					"Neural Interfaces",
					"Biometric Hardware",
					"Quantum Computing",
					"Virtual Reality Gear",
					"CCTV Equipments"
				};

				for (String name : defaultCategories) {
					try {
						if (categoryRepository.findByName(name) == null) {
							Category cat = new Category();
							cat.setName(name);
							categoryRepository.save(cat);
							System.out.println("Seeded category: " + name);
						}
					} catch (Exception e) {
						System.out.println("Category seeding skipped for: " + name);
					}
				}

				// Seed Admin User - check first to avoid duplicates
				try {
					Integer adminCount = jdbcTemplate.queryForObject(
						"SELECT COUNT(*) FROM users WHERE username = 'admin'", Integer.class);
					if (adminCount != null && adminCount > 1) {
						// Remove duplicates - keep only the one with lowest id
						jdbcTemplate.execute(
							"DELETE FROM users WHERE username = 'admin' AND id NOT IN (SELECT id FROM (SELECT MIN(id) as id FROM users WHERE username = 'admin') t)");
						System.out.println("Cleaned up duplicate admin users.");
					} else if (adminCount == null || adminCount == 0) {
						jdbcTemplate.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')");
						System.out.println("Seeded default admin user: admin / admin123");
					} else {
						System.out.println("Admin user already exists - skipping seed.");
					}
				} catch (Exception e) {
					System.out.println("Admin seeding/cleanup skipped: " + e.getMessage());
				}

				// Seed settings
				try {
					jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS settings (setting_key VARCHAR(255) PRIMARY KEY, setting_value VARCHAR(255))");
					Integer settingsCount = jdbcTemplate.queryForObject(
						"SELECT COUNT(*) FROM settings WHERE setting_key = 'admin_whatsapp'", Integer.class);
					if (settingsCount == null || settingsCount == 0) {
						jdbcTemplate.execute("INSERT INTO settings (setting_key, setting_value) VALUES ('admin_whatsapp', '94769414472')");
						System.out.println("Seeded default admin WhatsApp setting.");
					}
				} catch (Exception e) {
					System.out.println("Settings seeding skipped: " + e.getMessage());
				}

				// Seed Sample Products for Dashboard
				try {
					if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product", Integer.class) == 0) {
						Long cyberneticId = categoryRepository.findByName("Cybernetic Enhancements").getId();
						Long biometricId = categoryRepository.findByName("Biometric Hardware").getId();
						Long vrId = categoryRepository.findByName("Virtual Reality Gear").getId();
						Long cctvId = categoryRepository.findByName("CCTV Equipments").getId();

						// Insert Neural Link v2
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"Neural Link v2", 150000.0, "Next-gen brain interface updated via admin", cyberneticId, true, 10, null);

						// Insert Neural VR Headset
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"Neural VR Headset", 120000.0, "Fully immersive virtual reality headset with direct neural interface for hyper-realistic gaming.", vrId, false, 10, "https://images.unsplash.com/photo-1622979135225-d2ba269cf1ac?auto=format&fit=crop&w=600&q=80");

						// Insert Cybernetic Smartwatch
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"Cybernetic Smartwatch", 35000.0, "Advanced biometric tracking with AI-assisted daily planning and holographic projections.", biometricId, false, 9, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80");

						// Insert Drone Delivery System
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"Drone Delivery System", 250000.0, "Personal automated drone for instant neighborhood deliveries with 4K camera payload.", cyberneticId, false, 10, "https://images.unsplash.com/photo-1508614589041-895b88991e3e?auto=format&fit=crop&w=600&q=80");

						// Insert EZVIZ H9C 3K
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"EZVIZ H9C 3K", 23900.0, "The EZVIZ H9C 3K is a dual‑lens outdoor Wi‑Fi camera featuring 3K ultra‑clear video, 360° pan‑tilt coverage, AI human/vehicle detection, and smart auto‑tracking—designed to secure large areas with wide and close‑up views simultaneously.", cctvId, false, 10, "/product-images/e945fd16-ebcb-4e35-8e5d-d1b6fb16095b_OIP.webp");

						// Insert EZVIZ C3N
						jdbcTemplate.update("INSERT INTO product (name, price, description, category_id, featured, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
							"EZVIZ C3N", 19500.0, "Crafted utilizing state-of-the-art cybernetic engineering protocols with integrated neon power emitters. Fully optimized for lightspeed synchronization and seamless deployment in next-generation architectural grid arrays.", cctvId, false, 2, "/product-images/82a9dc4f-a4ff-49f1-967f-219b18464793_image-f3d2ce7cb1b540dca8f72dda3d44c76b.webp");

						System.out.println("Seeded all 6 sample products successfully.");
					} else {
						jdbcTemplate.execute("UPDATE product SET stock_quantity = 10 WHERE stock_quantity IS NULL");
					}
				} catch (Exception e) {
					System.out.println("Product seeding/stock update skipped: " + e.getMessage());
				}
			} catch (Exception e) {
				System.out.println("General seeding error: " + e.getMessage());
			}
		};
	}
}
