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
					"Virtual Reality Gear"
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

				// Seed Sample Products for Dashboard
				try {
					if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product", Integer.class) == 0) {
						Long catId = categoryRepository.findAll().get(0).getId();
						jdbcTemplate.execute("INSERT INTO product (name, price, description, category_id, featured, stock_quantity) VALUES ('Neural Link v2', 150000.0, 'Next-gen brain interface', " + catId + ", true, 10)");
						jdbcTemplate.execute("INSERT INTO product (name, price, description, category_id, featured, stock_quantity) VALUES ('Cyber Arm MK3', 450000.0, 'Titanium alloy limb', " + catId + ", true, 5)");
						System.out.println("Seeded sample products for dashboard.");
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
