package pe.edu.utp.Paway.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.utp.Paway.service.QuoteService;

@RestController
@RequestMapping("/api/v1/destinos")
public class DestinationApiController {
    private final QuoteService quotes;

    public DestinationApiController(QuoteService quotes) { this.quotes = quotes; }

    @GetMapping("/{province}/distritos")
    public List<String> districts(@PathVariable String province) {
        try {
            return quotes.destination(province).districts();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
