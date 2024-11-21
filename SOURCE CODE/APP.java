package com.supermarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

// Main Application Class
@SpringBootApplication
public class SupermarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupermarketApplication.class, args);
    }
}

// MongoDB Entity
@Document(collection = "items")
class Item {
    @Id
    private String id;
    private String name;
    private int quantity;
    private double price;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

// MongoDB Repository
interface ItemRepository extends MongoRepository<Item, String> {
    Optional<Item> findByName(String name);
    void deleteByName(String name);
}

// Controller
@Controller
@RequestMapping("/")
public class SupermarketController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/add")
    public String viewItems(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "view_items";
    }

    @GetMapping("/add_item")
    public String addItemForm() {
        return "add_items";
    }

    @PostMapping("/add_item")
    public String addItem(@RequestParam String name, @RequestParam int quantity, @RequestParam double price) {
        Item item = new Item();
        item.setName(name);
        item.setQuantity(quantity);
        item.setPrice(price);
        itemRepository.save(item);
        return "redirect:/add";
    }

    @GetMapping("/purchase_item")
    public String purchaseItemForm() {
        return "purchase_item";
    }

    @PostMapping("/purchase_item")
    public String purchaseItem(@RequestParam String name, Model model) {
        Optional<Item> optionalItem = itemRepository.findByName(name);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            if (item.getQuantity() > 0) {
                item.setQuantity(item.getQuantity() - 1);
                itemRepository.save(item);
                model.addAttribute("message", "Purchased " + name + " for $" + item.getPrice() + ".");
            } else {
                model.addAttribute("message", name + " is out of stock.");
            }
        } else {
            model.addAttribute("message", name + " not found in inventory.");
        }
        return "purchase_item";
    }

    @GetMapping("/search_item")
    public String searchItemForm() {
        return "search_item";
    }

    @PostMapping("/search_item")
    public String searchItem(@RequestParam String name, Model model) {
        Optional<Item> optionalItem = itemRepository.findByName(name);
        if (optionalItem.isPresent()) {
            model.addAttribute("item", optionalItem.get());
        } else {
            model.addAttribute("message", name + " not found in inventory.");
        }
        return "search_item";
    }

    @GetMapping("/edit_item")
    public String editItemForm() {
        return "edit_item";
    }

    @PostMapping("/edit_item")
    public String editItem(@RequestParam String oldName, @RequestParam String name,
                           @RequestParam int quantity, @RequestParam double price, Model model) {
        Optional<Item> optionalItem = itemRepository.findByName(oldName);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            item.setName(name);
            item.setQuantity(quantity);
            item.setPrice(price);
            itemRepository.save(item);
            model.addAttribute("message", oldName + " has been updated successfully.");
        } else {
            model.addAttribute("message", oldName + " not found in inventory.");
        }
        return "edit_item";
    }

    @GetMapping("/delete/{name}")
    public String deleteItem(@PathVariable String name) {
        itemRepository.deleteByName(name);
        return "redirect:/add";
    }
}
