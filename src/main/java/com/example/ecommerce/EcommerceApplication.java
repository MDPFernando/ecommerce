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
				System.out.println("Applying Database Schema Fix (MySQL Mode): product.description & offer.description -> TEXT");
				// Revert back to MySQL syntax
				try {
					jdbcTemplate.execute("ALTER TABLE product MODIFY COLUMN description TEXT");
					jdbcTemplate.execute("ALTER TABLE offer MODIFY COLUMN description TEXT");
					System.out.println("MySQL Schema fix applied successfully!");
				} catch (Exception e) {
					System.out.println("Schema fix skipped/failed (expected if tables not ready): " + e.getMessage());
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

				// Seed Admin User
				try {
					jdbcTemplate.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')");
					System.out.println("Seeded default admin user: admin / admin123");
				} catch (Exception e) {
					System.out.println("Admin seeding skipped: " + e.getMessage());
				}

				// Seed Sample Products for Dashboard
				try {
					if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product", Integer.class) == 0) {
						Long catId = categoryRepository.findAll().get(0).getId();
						jdbcTemplate.execute("INSERT INTO product (name, price, description, category_id, featured) VALUES ('Neural Link v2', 150000.0, 'Next-gen brain interface', " + catId + ", true)");
						jdbcTemplate.execute("INSERT INTO product (name, price, description, category_id, featured) VALUES ('Cyber Arm MK3', 450000.0, 'Titanium alloy limb', " + catId + ", true)");
						System.out.println("Seeded sample products for dashboard.");
					}
				} catch (Exception e) {
					System.out.println("Product seeding skipped: " + e.getMessage());
				}
			} catch (Exception e) {
				System.out.println("General seeding error: " + e.getMessage());
			}
		};
	}
}
